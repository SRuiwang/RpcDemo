package com.sr.rpc.server;

import com.sr.commons.RpcDecode;
import com.sr.commons.RpcEncode;
import com.sr.commons.RpcRequest;
import com.sr.zkregistry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RpcServer.class);

    private String serverAddress;
    private ServiceRegistry serviceRegistry;

    //用于存储业务接口和实现类的实例对象(由spring所构造)
    private Map<String, Object> handlerMap = new HashMap<String, Object>();

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new RpcDecode(RpcRequest.class))
                                    .addLast(new RpcEncode(RpcRequest.class))
                                    .addLast(new ServerHandle(handlerMap));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] split = serverAddress.split(":");
            String host = split[0];
            int port = Integer.parseInt(split[1]);
            ChannelFuture future = bootstrap.bind(host, port).sync();
            LOGGER.debug("server start on port： {}", port);

            if (serviceRegistry != null) {
                serviceRegistry.registry(serverAddress);
            }
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(RpcSerivce.class);
        if (MapUtils.isNotEmpty(beanMap)) {
            for (Object bean : beanMap.values()) {
                String interfaceName = bean.getClass().getAnnotation(RpcSerivce.class).value().getName();
                handlerMap.put(interfaceName, bean);
            }
        }

    }
}