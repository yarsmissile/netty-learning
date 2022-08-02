package com.github.yarsmissile.nio;

import com.github.yarsmissile.handler.EchoServerHandler;
import io.netty5.bootstrap.ServerBootstrap;
import io.netty5.channel.*;
import io.netty5.channel.nio.NioHandler;
import io.netty5.channel.socket.SocketChannel;
import io.netty5.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class NioHelloServer {
    private static final Logger logger = LoggerFactory.getLogger(NioHelloServer.class);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        if (args.length < 1) {
            throw new RuntimeException("请输入端口号");
        }
        int port = Integer.parseInt(args[0]);
        EventLoopGroup bossGroup = new MultithreadEventLoopGroup(1, NioHandler.newFactory());
        EventLoopGroup workerGroup = new MultithreadEventLoopGroup(4, NioHandler.newFactory());
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });
            Channel channel = serverBootstrap.bind(port).asStage().get();
            logger.info("服务器上线");
            channel.closeFuture().asStage().sync();
            logger.info("服务器退出");
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
