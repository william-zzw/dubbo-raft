package com.hongframe.raft.rpc.core;

import com.hongframe.raft.NodeManager;
import com.hongframe.raft.core.NodeImpl;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.entity.PeerId;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-16 20:05
 */
public interface RpcService {

    default NodeImpl getNode(Message message) {
        PeerId peerId = new PeerId();
        peerId.parse(message.getPeerId());
        return (NodeImpl) NodeManager.getInstance().get(message.getGroupId(), peerId);
    }

}
