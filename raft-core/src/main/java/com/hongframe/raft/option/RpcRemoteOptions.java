package com.hongframe.raft.option;

import com.hongframe.raft.Node;
import com.hongframe.raft.rpc.core.AppendEntriesRpc;
import com.hongframe.raft.rpc.core.RequestVoteRpc;
import com.hongframe.raft.rpc.impl.AppendEntriesRpcImpl;
import com.hongframe.raft.rpc.impl.RequestVoteRpcImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 23:39
 */
public class RpcRemoteOptions {

    private Node node;
    private List<Class> servicesInterface = new ArrayList<>();
    private List<Class> servicesImpl = new ArrayList<>();

    public RpcRemoteOptions() {
        init();
    }

    private void init() {
        addRaftRequest();
    }

    private void addRaftRequest() {
        addRaftRequest0(RequestVoteRpc.class, RequestVoteRpcImpl.class);
        addRaftRequest0(AppendEntriesRpc.class, AppendEntriesRpcImpl.class);
    }

    private void addRaftRequest0(Class interfacez, Class implz) {
        servicesInterface.add(interfacez);
        servicesImpl.add(implz);
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public List<Class> getServicesInterface() {
        return servicesInterface;
    }

    public List<Class> getServicesImpl() {
        return servicesImpl;
    }
}
