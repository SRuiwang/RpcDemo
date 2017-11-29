package com.sr;

import com.sr.commons.RpcRequest;
import com.sr.commons.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Authot: shaorui
 * @Description: 处理具体的业务调用
 * 通过构造时传入的“业务接口及实现”handlerMap，来调用客户端所请求的业务方法
 * 并将业务方法返回值封装成response对象写入下一个handler（即编码handler——RpcEncoder）
 * @Date: 13:29 2017/11/29
 */
public class ServerHandle extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ServerHandle.class);

    private Map<String, Object> handlerMap;

    public ServerHandle(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    /**
     * @Authot: shaorui
     * @Description: 接收消息，处理消息，返回结果
     * @Date: 13:31 2017/11/29
     */
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(request.getRequestId());
        try {
            Object result = handel(request);
            rpcResponse.setResult(result);
        } catch (Throwable t) {
            rpcResponse.setException(t);
        }
        ctx.writeAndFlush(rpcResponse);
    }

    private Object handel(RpcRequest request) throws Throwable {
        Object o = handlerMap.get(request.getInterfaceName());
        Class<?> clazz = Class.forName(request.getInterfaceName());
        Method method = clazz.getMethod(request.getMethod(), request.getParamTypes());
        return method.invoke(o, request.getParams());
    }
}