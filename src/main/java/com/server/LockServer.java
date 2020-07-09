package com.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codec.MarshallingCodeCFactory;
import com.server.handler.LockServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class LockServer {
    static Logger log = LoggerFactory.getLogger(LockServer.class);
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();

                        // marshlling编解码器
                        pipeline.addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
                        pipeline.addLast(MarshallingCodeCFactory.buildMarshallingEncoder());

                        // 自定义处理器
                        pipeline.addLast(new LockServerHandler());
                    }
                });

        try {
            ChannelFuture future = b.bind(8899).sync();
            future.channel().closeFuture().addListener((f) -> {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            log.error("绑定端口错误!", e);
        }
    }
}
