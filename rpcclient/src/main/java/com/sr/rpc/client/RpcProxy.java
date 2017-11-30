package com.sr.rpc.client;

import com.sr.commons.RpcRequest;
import com.sr.commons.RpcResponse;
import com.sr.zkregistry.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @Authot: shaorui
 * @Description: RPC 代理（用于创建 RPC 服务代理）
 * @Date: 16:38 2017/11/30
 */
public class RpcProxy implements InvocationHandler {
    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> T create(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, RpcProxy.this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setInterfaceName(method.getDeclaringClass().getName());
        rpcRequest.setMethod(method.getName());
        rpcRequest.setParamTypes(method.getParameterTypes());
        rpcRequest.setParams(args);

        //查找服务

        if (serviceDiscovery != null) {
            serverAddress = serviceDiscovery.discover();
        }
        String[] array = serverAddress.split(":");
        String host = array[0];
        int port = Integer.parseInt(array[1]);

        RpcClient rpcClient = new RpcClient(host, port);
        RpcResponse rpcResponse = rpcClient.Send(rpcRequest);
        if (rpcResponse.isError()) {
            throw rpcResponse.getException();
        } else {
            return rpcResponse.getResult();
        }
    }
}