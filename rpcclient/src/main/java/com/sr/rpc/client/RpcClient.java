package com.sr.rpc.client;

import com.sr.commons.RpcDecode;
import com.sr.commons.RpcEncode;
import com.sr.commons.RpcRequest;
import com.sr.commons.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RpcClient.class);

    private String host;
    private int port;

    private Object object = new Object();//锁，等待调用结果返回后释放

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private RpcResponse rpcResponse;

    public RpcResponse Send(RpcRequest rpcRequest) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            .addLast(new RpcEncode(RpcRequest.class))
                            .addLast(new RpcDecode(RpcResponse.class))
                            .addLast(RpcClient.this);
                }
            }).option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.connect(host, port).sync();
            synchronized (object) {
                object.wait();
            }
            if (rpcResponse != null) {
                future.channel().closeFuture().sync();
            }
            return rpcResponse;
        } finally {
            group.shutdownGracefully();
        }
    }


    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        this.rpcResponse = msg;
        synchronized (object) {
            object.notifyAll();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }

}