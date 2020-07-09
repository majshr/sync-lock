package com.client.connection;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.Config;
import com.client.lock.DLockImpl;
import com.client.lock.ResponseFuture;
import com.model.Request;
import com.model.Response;
import com.model.Steps;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

/**
 * @author mengaijun
 * @Description: 连接客户端
 * @date: 2020年3月25日 下午5:30:58
 */
public class DLockClient extends AbstractClient {
    Logger log = LoggerFactory.getLogger(DLockClient.class);

    private DLockImpl lock;

    /** 维护响应信息(根据ID对应) */
    private ConcurrentHashMap<Long, ResponseFuture> responseMap = new ConcurrentHashMap<>();

    public DLockClient(String host, int port, DLockImpl lock) {
        super(host, port);
        this.lock = lock;
    }

    public DLockClient(Config config, DLockImpl lock) {
        this(config.getHost(), config.getPort(), lock);
    }

    /**
     * 连接操作
     * 
     * @date: 2020年3月25日 下午5:34:13
     */
    public void connect() {
        bootstrap();
        doConnect();
    }

    /**
     * 发送请求, 获取响应Future
     * 
     * @param request
     * @return ResponseFuture
     * @date: 2020年3月25日 下午5:35:22
     */
    public ResponseFuture sendRequest(Request request) {
        long lockId = request.getLockId();
        ResponseFuture responseFuture = new ResponseFuture(request);
        responseMap.put(lockId, responseFuture);
        channel.writeAndFlush(request);
        return responseFuture;
    }

    /**
     * 发送请求, 不设置响应
     * 
     * @param request
     * @date: 2020年3月26日 上午10:43:40
     */
    public void sendRequestWithoutResponse(Request request) {
        channel.writeAndFlush(request);
    }

    // 加入LockHandler
    @Override
    protected void initSocketChannel(SocketChannel channel) {
        // 添加handler
        channel.pipeline().addLast(new LockHandler(lock));
    }

    /**
     * 
     * @author mengaijun
     * @Description: 处理器
     * @date: 2020年3月25日 下午5:50:25
     */
    class LockHandler extends SimpleChannelInboundHandler<Response> {

        private DLockImpl lock;

        public LockHandler(DLockImpl lock) {
            this.lock = lock;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Response res) throws Exception {
            switch (res.getOperate()) {
                case Connect: {
                // 连接操作, 设置进程号(此时建立连接成功)
                    lock.revisionProcessId(res.getProcessId());
                    break;
                }
                case Unlock:
                case Lock:
                case TryLock:{
                    // 响应回调给future
                    ResponseFuture responseFuture = responseMap.remove(res.getLockId());
                    if(responseFuture != null) {
                        responseFuture.setResponse(res);    
                    }
                    break;
                }
                default: {
                    log.debug("ignore unknown operate:{}", res.getOperate());
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // 连接成功后, 发送连接请求, 注册本进程
            channel = ctx.channel();
            Request connectRequest = new Request(-1L, -1L, Steps.Connect, null);
            channel.writeAndFlush(connectRequest);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("channelInactive:{}", ctx.channel());
            super.channelInactive(ctx);
            // 通道不可用后, 服务下线
            lock.serverBreak();
            responseMap.values().forEach(v->{
                v.inactive();
            });
            doConnect();
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            log.error("exceptionCaught", cause);
        }
    }

}
