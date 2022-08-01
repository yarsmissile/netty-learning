package com.github.yarsmissile.nio;

import com.github.yarsmissile.handler.EchoClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioHelloClient {
    private static final Logger logger = LoggerFactory.getLogger(NioHelloClient.class);

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 2) {
            throw new RuntimeException("请输入要连接的服务器IP和端口号");
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
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
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            logger.info("客户端上线");
            channelFuture.channel().closeFuture().sync();
            logger.info("客户端退出");
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
