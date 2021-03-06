package com.hongframe.raft.counter;

import com.hongframe.raft.Iterator;
import com.hongframe.raft.StateMachine;
import com.hongframe.raft.Status;
import com.hongframe.raft.callback.Callback;
import com.hongframe.raft.storage.snapshot.SnapshotReader;
import com.hongframe.raft.storage.snapshot.SnapshotWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-05-08 16:50
 */
public class CounterStateMachine implements StateMachine {

    private static final Logger LOG = LoggerFactory.getLogger(CounterStateMachine.class);

    private final AtomicLong value = new AtomicLong(0);

    private final AtomicLong leaderTerm = new AtomicLong(-1);

    public boolean isLeader() {
        return this.leaderTerm.get() > 0;
    }

    @Override
    public void onApply(Iterator iterator) {
        if (!isLeader()) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        CounterCallback callback = null;
        while (iterator.hasNext()) {
            if (iterator.callback() != null) {
                callback = (CounterCallback) iterator.callback();
            }
            final ByteBuffer data = iterator.data();
            int remaining = data.remaining();
            long d = data.getLong();
            value.addAndGet(d);
            LOG.info("data: {}, remaining: {}, value: {}", d, remaining, value.get());
            if (callback != null) {
                callback.success(value.get());
                callback.run(Status.OK());
            }
            iterator.next();
        }
    }

    public long getValue() {
        return this.value.get();
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void onLeaderStart(long term) {
        LOG.info("onLeaderStart: term={}.", term);
        leaderTerm.set(term);
    }

    @Override
    public void onLeaderStop(Status status) {

    }

    @Override
    public void onSnapshotSave(SnapshotWriter writer, Callback callback) {
        LOG.info("onSnapshotSave");
        final long currVal = this.value.get();
        final CounterSnapshotFile snapshot = new CounterSnapshotFile(writer.getPath() + File.separator + "data");
        if (snapshot.save(currVal)) {
            if (writer.addFile("data")) {
                callback.run(Status.OK());
            } else {

            }
        } else {

        }
    }

    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        if (isLeader()) {
            LOG.warn("Leader is not supposed to load snapshot");
            return false;
        }
        if (reader.getFileMeta("data") == null) {
            LOG.error("Fail to find data file in {}", reader.getPath());
            return false;
        }

        final CounterSnapshotFile snapshot = new CounterSnapshotFile(reader.getPath() + File.separator + "data");
        try {
            this.value.set(snapshot.load());
            return true;
        } catch (final IOException e) {
            LOG.error("Fail to load snapshot from {}", snapshot.getPath());
            return false;
        }
    }
}
