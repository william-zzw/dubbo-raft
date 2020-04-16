package com.hongframe.raft.rpc.core;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 20:05
 */
public interface MembershipChange extends RpcService {

    void addPeer();

    void removePeer();

    void changePeer();

    void resetPeer();

    void transferLeadershipTo();

}
