package com.sr.commons;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcDecode extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public RpcDecode(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        int dataLength = byteBuf.readInt();
        byte[] bytes = new byte[dataLength];
        byteBuf.readBytes(bytes);
        byteBuf.clear();
        Object deserialize = SerializationUtil.deserialize(bytes, genericClass);
        list.add(deserialize);
    }
}