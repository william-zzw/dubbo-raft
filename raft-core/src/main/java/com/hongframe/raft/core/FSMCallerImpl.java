package com.hongframe.raft.core;

import com.hongframe.raft.FSMCaller;
import com.hongframe.raft.StateMachine;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.callback.CallbackQueue;
import com.hongframe.raft.option.FSMCallerOptions;
import com.hongframe.raft.storage.LogManager;
import com.hongframe.raft.util.DisruptorBuilder;
import com.hongframe.raft.util.LogExceptionHandler;
import com.hongframe.raft.util.NamedThreadFactory;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-27 20:04
 */
public class FSMCallerImpl implements FSMCaller {

    private StateMachine stateMachine;
    private LogManager logManager;
    private NodeImpl node;
    private final AtomicLong applyingIndex;
    private long lastAppliedTerm;
    private final AtomicLong lastAppliedIndex;
    private CallbackQueue callbackQueue;
    private Disruptor<CallerTask> disruptor;
    private RingBuffer<CallerTask> taskQueue;

    public FSMCallerImpl() {
        super();
        this.lastAppliedIndex = new AtomicLong(0);
        this.applyingIndex = new AtomicLong(0);
    }

    private enum TaskType {
        IDLE,
        COMMITTED,
        ERROR,
        ;
    }

    private class CallerTask {
        TaskType type;
        long committedIndex;
        long term;

        public void reset() {
            this.type = null;
            this.committedIndex = 0;
            this.term = 0;
        }
    }

    private class CallerTaskFactory implements EventFactory<CallerTask> {

        @Override
        public CallerTask newInstance() {
            return new CallerTask();
        }
    }

    private class CallerTaskHandler implements EventHandler<CallerTask> {
        private long maxCommittedIndex = -1;

        @Override
        public void onEvent(CallerTask event, long sequence, boolean endOfBatch) throws Exception {
            this.maxCommittedIndex = runApplyTask(event, this.maxCommittedIndex, endOfBatch);
        }
    }

    @Override
    public boolean init(FSMCallerOptions opts) {
        this.stateMachine = opts.getFsm();
        this.logManager = opts.getLogManager();
        this.node = opts.getNode();
        this.callbackQueue = opts.getCallbackQueue();

        this.disruptor = DisruptorBuilder.<CallerTask>newInstance() //
                .setEventFactory(new CallerTaskFactory()) //
                .setRingBufferSize(opts.getDisruptorBufferSize()) //
                .setThreadFactory(new NamedThreadFactory("Dubbo-Raft-FSMCaller-Disruptor-", true)) //
                .setProducerType(ProducerType.MULTI) //
                .setWaitStrategy(new BlockingWaitStrategy()) //
                .build();
        this.disruptor.handleEventsWith(new CallerTaskHandler());
        this.disruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.taskQueue = this.disruptor.start();
        return true;
    }

    @Override
    public boolean onCommitted(long committedIndex) {
        EventTranslator<CallerTask> tpl = (task, sequence) -> {
            task.committedIndex = committedIndex;
            task.type = TaskType.COMMITTED;
        };
        if (!this.taskQueue.tryPublishEvent(tpl)) {
            return false;
        }
        return true;
    }

    private long runApplyTask(final CallerTask task, long maxCommittedIndex, final boolean endOfBatch) {
        if (task.type == TaskType.COMMITTED) {
            if (task.committedIndex > maxCommittedIndex) {
                maxCommittedIndex = task.committedIndex;
            }
        }
        if (maxCommittedIndex > -1 && endOfBatch) {
            doCommitted(maxCommittedIndex);
            maxCommittedIndex = -1L;
        }
        return maxCommittedIndex;
    }

    private void doCommitted(final long committedIndex) {
        final List<Callback> callbacks = new ArrayList<>();
        long firstIndex = this.callbackQueue.popClosureUntil(committedIndex, callbacks);
        //TODO doCommitted
    }

    @Override
    public void shutdown() {

    }
}
