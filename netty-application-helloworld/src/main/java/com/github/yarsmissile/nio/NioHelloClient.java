package com.github.yarsmissile.nio;

import com.github.yarsmissile.handler.EchoClientHandler;

import io.netty5.bootstrap.Bootstrap;
import io.netty5.channel.Channel;
import io.netty5.channel.ChannelInitializer;
import io.netty5.channel.EventLoopGroup;
import io.netty5.channel.MultithreadEventLoopGroup;
import io.netty5.channel.nio.NioHandler;
import io.netty5.channel.socket.SocketChannel;
import io.netty5.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class NioHelloClient {
    private static final Logger logger = LoggerFactory.getLogger(NioHelloClient.class);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        if (args.length < 2) {
            throw new RuntimeException("请输入要连接的服务器IP和端口号");
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        EventLoopGroup eventLoopGroup = new MultithreadEventLoopGroup(NioHandler.newFactory());
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            Channel channel = bootstrap.connect(host, port).asStage().get();
            logger.info("客户端上线");
            channel.closeFuture().asStage().sync();
            logger.info("客户端退出");
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
