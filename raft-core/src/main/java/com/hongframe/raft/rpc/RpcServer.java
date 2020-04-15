package com.hongframe.raft.rpc;

import com.hongframe.raft.option.RpcRemoteOptions;
import com.hongframe.raft.rpc.core.RequestVoteRpc;
import com.hongframe.raft.rpc.impl.RequestVoteRpcImpl;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 墨声 E-mail: zehong.hongframe.huang@gmail.com
 * @version create time: 2020-04-15 21:11
 */
public class RpcServer {

    private Integer port;

    private RpcRemoteOptions rpcRemoteOptions;

    private DubboBootstrap dubboBootstrap;



    public RpcServer(int port, RpcRemoteOptions options) {
        this.port = port;
        this.rpcRemoteOptions = options;
    }

    public void init() {
        List<ServiceConfig> services = new ArrayList<>();
        try {
            List<Class> servicesInterface = this.rpcRemoteOptions.getServicesInterface();
            List<Class> servicesImpl = this.rpcRemoteOptions.getServicesImpl();
            for (int i = 0; i < servicesInterface.size(); i++) {
                ServiceConfig service = new ServiceConfig<>();
                service.setInterface(servicesInterface.get(i));
                service.setRef(servicesImpl.get(i).newInstance());
                service.setAsync(true);
                services.add(service);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        this.dubboBootstrap = DubboBootstrap.getInstance();

        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setPort(port);

        new Thread(() -> {
            dubboBootstrap.application(new ApplicationConfig("dubbo-demo-api-provider"))
                    .registry(new RegistryConfig("N/A"))
                    .services(services)
                    .protocol(protocol)
                    .start()
                    .await();
        }).start();
    }




}
