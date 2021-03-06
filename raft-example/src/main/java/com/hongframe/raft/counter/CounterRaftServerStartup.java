package com.hongframe.raft.counter;

import com.hongframe.raft.DubboRaftRpcFactory;
import com.hongframe.raft.Node;
import com.hongframe.raft.RaftGroupService;
import com.hongframe.raft.conf.Configuration;
import com.hongframe.raft.counter.rpc.CounterService;
import com.hongframe.raft.counter.rpc.CounterServiceImpl;
import com.hongframe.raft.entity.PeerId;
import com.hongframe.raft.option.NodeOptions;
import com.hongframe.raft.option.RaftOptions;
import com.hongframe.raft.option.ReadOnlyOption;
import com.hongframe.raft.rpc.RpcServer;
import com.hongframe.raft.util.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-24 22:32
 */
public class CounterRaftServerStartup {

    private static final Logger LOG = LoggerFactory.getLogger(CounterRaftServerStartup.class);

    public static final String NODES = "localhost:8888,localhost:8889,localhost:8890";

    public static final String GROUP = "raft";

    private Node node;

    private CounterStateMachine fsm;


    private Node startup(int port, String servers) {

        Endpoint endpoint = new Endpoint("localhost", port);
        PeerId serverId = new PeerId(endpoint, 0);


        RpcServer rpcServer = DubboRaftRpcFactory.createRaftRpcServer(endpoint);
        rpcServer.registerUserService(CounterService.class, new CounterServiceImpl(this));

        Configuration configuration = new Configuration();
        configuration.parse(servers);

        NodeOptions nodeOptions = new NodeOptions();

        RaftOptions raftOptions = new RaftOptions();
        raftOptions.setReadOnlyOptions(ReadOnlyOption.ReadOnlyLeaseBased);
        raftOptions.setMaxReplicatorFlyingMsgs(8);
        nodeOptions.setRaftOptions(raftOptions);

        nodeOptions.setConfig(configuration);
        nodeOptions.setLogUri("." + File.separator + "__data");
        nodeOptions.setSnapshotUri("." + File.separator + "__data");
        nodeOptions.setSnapshotIntervalSecs(60);

        this.fsm = new CounterStateMachine();
        nodeOptions.setStateMachine(this.fsm);

        RaftGroupService raftGroupService = new RaftGroupService(GROUP, serverId, nodeOptions, rpcServer);
        this.node = raftGroupService.start();
        LOG.info("started...");

        return node;
    }

    public CounterStateMachine getFsm() {
        return fsm;
    }

    private CounterRaftServerStartup(int port, String servers) {
        this.node = startup(port, servers);
    }

    public static CounterRaftServerStartup create(int port, String servers) {
        return new CounterRaftServerStartup(port, servers);
    }

    public Node getNode() {
        return node;
    }
}
