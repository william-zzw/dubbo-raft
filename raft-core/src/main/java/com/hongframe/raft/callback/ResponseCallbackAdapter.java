package com.hongframe.raft.callback;

import com.hongframe.raft.Status;
import com.hongframe.raft.callback.ResponseCallback;
import com.hongframe.raft.entity.Message;
import com.hongframe.raft.rpc.RpcRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * create time: 2020-04-18 15:51
 */
public abstract class ResponseCallbackAdapter implements ResponseCallback {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseCallbackAdapter.class);

    private Message message ;

    public void setResponse(Message message) {
        this.message = message;
    }

    @Override
    public Message getResponse() {
        return this.message;
    }

    public void invoke(RpcRequests.Response response) {
        Message message = response.getData();
        if(Objects.nonNull(message)) {
            setResponse(message);
            if(message instanceof RpcRequests.ErrorResponse) {
                RpcRequests.ErrorResponse error = (RpcRequests.ErrorResponse) message;
                run(new Status(error.getErrorCode(), error.getErrorMsg()));
            } else {
                run(Status.OK());
            }
        } else {
            run(new Status(response.getError().getErrorCode(), response.getError().getErrorMsg()));
        }
    }
}
