package com.github.yarsmissile.handler;


import io.netty5.buffer.api.Buffer;
import io.netty5.channel.ChannelHandler;
import io.netty5.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public class EchoClientHandler implements ChannelHandler {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Buffer buffer = ctx.bufferAllocator().copyOf("你好，我是客户端", StandardCharsets.UTF_8);
        ctx.writeAndFlush(buffer);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
