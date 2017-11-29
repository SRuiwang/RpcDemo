package com.sr.commons;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncode extends MessageToByteEncoder {

    private Class<?> genericClass;

    public RpcEncode(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if(genericClass.isInstance(o)){
            byte[] serialize = SerializationUtil.serialize(o);
            byteBuf.writeInt(serialize.length);
            byteBuf.writeBytes(serialize);
        }

    }
}