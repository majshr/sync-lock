package com.server.handler;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.model.Request;
import com.model.Response;
import com.model.ResponseCode;
import com.model.Steps;
import com.server.tool.Lock;
import com.server.tool.LockCarrier;
import com.server.tool.Operate;
import com.server.tool.Process;
import com.server.tool.ProcessCarrier;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 服务端handler
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月24日 上午11:04:03
 */
public class LockServerHandler extends SimpleChannelInboundHandler<Request> {
    static Logger log = LoggerFactory.getLogger(LockServerHandler.class);

    ProcessCarrier processCarrier = ProcessCarrier.getInstance();
    LockCarrier lockCarrier = LockCarrier.getInstance();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        switch (request.getOperate()) {
        case Connect:
            connectAction(ctx, request);
            break;
        case Lock:
            lockAction(ctx, request);
            break;
        case TryLock:
            tryLockAction(ctx, request);
            break;
        case Unlock:
            unlockAction(ctx, request);
            break;
        case Close:
            break;
        default:
            Response response = new Response(request.getOperate(), -1L, -1L, null, ResponseCode.Fail);
            response.setMessage("unknown the operate:" + request.getOperate());
            ctx.writeAndFlush(response);
            break;
        }
            
    }

    /**
     * 连接操作(保存process到本地, 生成processId, 发送响应)
     * 
     * @param ctx
     * @param request
     * @date: 2020年3月25日 上午11:42:41
     */
    private void connectAction(ChannelHandlerContext ctx, Request request) {
        Process process = new Process(ctx.channel());
        processCarrier.addProcess(process);
        Response response = new Response(Steps.Connect, process.getProcessId(), -1L, null, ResponseCode.Ok);
        ctx.writeAndFlush(response);
    }

    /**
     * 尝试获取锁操作
     * 
     * @param ctx
     * @param request
     * @date: 2020年3月25日 上午11:43:47
     */
    private void tryLockAction(ChannelHandlerContext ctx, Request request) {
        Channel channel = ctx.channel();
        Operate operate = new Operate(request.getProcessId(), request.getLockId(), request.getResource(), channel);
        boolean tryAcquire = lockCarrier.tryAcquire(operate);
        Response response = new Response(Steps.TryLock, operate.getProcessId(), operate.getLockId(),
                operate.getResource(), tryAcquire ? ResponseCode.Ok : ResponseCode.Fail);
        channel.writeAndFlush(response);
    }

    /**
     * 获取锁操作
     * 
     * @param ctx
     * @param request
     * @date: 2020年3月25日 上午11:54:29
     */
    private void lockAction(ChannelHandlerContext ctx, Request request) {
        Channel channel = ctx.channel();
        Operate operate = new Operate(request.getProcessId(), request.getLockId(), request.getResource(), channel);
        if (lockCarrier.acquire(operate)) {
            // 获取锁成功
            Response response = new Response(Steps.TryLock, operate.getProcessId(), operate.getLockId(),
                    operate.getResource(), ResponseCode.Ok);
            channel.writeAndFlush(response);
            return;
        }

        // 获取锁失败的话, 客户端会阻塞等待锁, 直到超时; 或者获得锁成功的任务释放锁, 之后下一个operate得到锁, 通知客户端
    }
    
    /**
     * unlock动作(unlock完后, 通知下一个获取到锁的线程nextOperate)
     * 
     * @param ctx
     * @param request
     * @date: 2020年3月25日 下午2:25:19
     */
    private void unlockAction(ChannelHandlerContext ctx, Request request) {
        Channel channel = ctx.channel();
        Operate operate = new Operate(request.getProcessId(), request.getLockId(), request.getResource(), channel);
        unlockAction(ctx, operate);
    }

    /**
     * unlock操作
     * 
     * @param ctx
     * @param operate
     * @date: 2020年3月27日 上午11:30:09
     */
    private void unlockAction(ChannelHandlerContext ctx, Operate operate) {
        Operate nextOperate = lockCarrier.release(operate);
        // nextOperate得到资源
        if (nextOperate != null) {
            Channel nextOperatorChannel = nextOperate.getChannel();
            Response response = new Response(Steps.Lock, nextOperate.getProcessId(), nextOperate.getLockId(),
                    nextOperate.getResource(), ResponseCode.Ok);
            nextOperatorChannel.writeAndFlush(response);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 连接断开后, 如果有当前获取到锁的Operate属于断开的连接, 会导致它不能再被释放, 需要服务器端手动释放
        // 遍历所有当前获取到锁的操作, 判断channel如果inactive了, 释放锁
        // 启动个新线程进行操作, 避免阻塞netty线程
        new Thread(() -> {
            Map<String, Lock> lockMap = lockCarrier.getInstance().peekLockMap();
            lockMap.forEach((str, lock) -> {
                Operate operate = lock.getCurrentOperate();

                // 没有操作获取到锁, 不处理
                if (operate == null) {
                    return;
                }

                // 如果channel被断开了, unlock一下
                if (!operate.getChannel().isActive()) {
                    if (Objects.equals(operate, lock.getCurrentOperate())) {
                        unlockAction(ctx, operate);
                    }
                }
            });
        }).start();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }
}
