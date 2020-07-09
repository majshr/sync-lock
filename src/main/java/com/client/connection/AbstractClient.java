package com.client.connection;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.Config;
import com.codec.MarshallingCodeCFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * 抽象客户端(实现连接服务端操作)
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月27日 下午5:24:31
 */
public abstract class AbstractClient {
    static Logger log = LoggerFactory.getLogger(AbstractClient.class);

    protected String host;
    protected int port;

    protected EventLoopGroup group;
    protected Bootstrap bootstrap;
    protected volatile Channel channel;

    public AbstractClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public AbstractClient(Config config) {
        this(config.getHost(), config.getPort());
    }

    /**
     * 设置启动连接信息
     * 
     * @date: 2020年3月25日 下午5:09:55
     */
    protected void bootstrap() {
        group = new NioEventLoopGroup(NettyRuntime.availableProcessors() * 2, new DefaultThreadFactory("dsync-client"));
        bootstrap = new Bootstrap().group(group)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        // pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                        // marshlling编解码器
                        pipeline.addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
                        pipeline.addLast(MarshallingCodeCFactory.buildMarshallingEncoder());
                        // 初始化channel
                        initSocketChannel(ch);
                    }
                });

        // 程序挂的话, 停止线程组
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(), "DSync-JVM-shutdown-hook"));
    }

    /**
     * 关闭
     * 
     * @date: 2020年3月25日 下午5:09:04
     */
    public void shutdown() {
        group.shutdownGracefully();
    }

    /**
     * 连接操作
     * 
     * @date: 2020年3月25日 下午5:11:20
     */
    protected void doConnect() {
        if (channel != null && channel.isActive()) {
            return;
        }

        log.info("Connect to server: {}:{}", host, port);
        ChannelFuture future = bootstrap.connect(host, port);

        future.addListener((ChannelFutureListener) futureListener -> {
            if (futureListener.isSuccess()) {
                log.info("Connect to server successfully!");
            } else {
                // 连接失败, 三秒之后, 重试连接
                log.warn("Failed to connect to server, try connect after 3s");
                futureListener.channel().eventLoop().schedule(() -> doConnect(), 3, TimeUnit.SECONDS);
            }
        });
    }

    /**
     * 初始化SocketChannel
     * 
     * @param channel
     * @date: 2020年3月25日 下午5:16:23
     */
    protected abstract void initSocketChannel(SocketChannel channel);

}
