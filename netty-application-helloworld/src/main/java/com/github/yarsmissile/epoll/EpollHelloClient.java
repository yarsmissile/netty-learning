package com.github.yarsmissile.epoll;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EpollHelloClient {
    static {
        System.loadLibrary("netty-transport-native-epoll-x86_64");
    }

    private static final Logger logger = LoggerFactory.getLogger(EpollHelloClient.class);

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            throw new RuntimeException("请输入要连接的服务器IP和端口号");
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        EventLoopGroup eventLoopGroup = new EpollEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(EpollSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast();
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channelFuture.channel().closeFuture().sync();
            logger.info("客户端退出");
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
