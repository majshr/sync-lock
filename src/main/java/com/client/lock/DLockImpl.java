package com.client.lock;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.Config;
import com.client.DLock;
import com.client.connection.DLockClient;
import com.model.Request;
import com.model.Response;
import com.model.ResponseCode;
import com.model.Steps;

/**
 * DLock实现(实现了lock相关操作)
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月25日 下午5:25:57
 */
public class DLockImpl implements DLock {
    Logger log = LoggerFactory.getLogger(DLockImpl.class);

    /** 连接客户端 */
    private DLockClient client;

    private volatile long processId;
    private CountDownLatch initProcessLatch = new CountDownLatch(1);

    /**
     * lock成功后, 会在当前线程设置request信息<br>
     * unlock时会检查, unlock对应的request, 保证lock和unlock是请求为同一个id
     */
    private ThreadLocal<Request> threadLocal = new ThreadLocal<>();

    /**
     * lockId自增器, 生成每次请求唯一标识
     */
    private AtomicLong lockIdAdder = new AtomicLong(1);

    public DLockImpl(Config config) {
        this.client = new DLockClient(config, this);
    }

    /**
     * 初始化(与服务端建立连接)
     * 
     * @date: 2020年3月26日 上午10:21:11
     */
    public void init() {
        client.connect();
    }

    // 尝试获取锁, 获取不到立即返回false
    // 获取不到的话, 服务端也不会加入到等待锁的队列, 不需要解锁
    // 获取到的话, 返回true, 需要解锁
    @Override
    public boolean tryLock(String resource) {
        log.debug("try lock resource:{}", resource);
        long processId = syncGetProcessId();

        long lockId = createLockId();

        Request request = new Request(processId, lockId, Steps.TryLock, resource);
        ResponseFuture responseFuture = client.sendRequest(request);

        try {
            Response response = responseFuture.get();
            log.info("try lock resource is:{}", response);
            if (ResponseCode.Ok.equals(response.getCode())) {
                // 获取成功, 需要解锁, 设置request的ThreadLocal
                threadLocal.set(request);
                return true;
            }
            return false;

        } catch (InterruptedException e) {
            log.error("try lock resume error", e);
            return false;
        }
    }

    /**
     * 超时的到期进行unlock之前, 超时的operate被当做了当前操作, 就是unlock当前操作<br>
     * 超时的到期进行unlock操作, 超时的operate还在等待队列中, 就是队列中取出操作
     */
    // 超时时间内获取锁
    // 超时时间内, 获取到, 获取成功, 返回true
    // 超时时间内, 没有获取到, 获取失败, 需要unlock操作(取消等待)
    @Override
    public boolean tryLock(String resource, Duration duration) {
        log.debug("lock resource:{}", resource);
        long processId = syncGetProcessId();

        long lockId = createLockId();

        Request request = new Request(processId, lockId, Steps.Lock, resource);
        log.info("try get lock request:{}", request);
        ResponseFuture responseFuture = client.sendRequest(request);
        try {
            // 如果开始没获取到锁, 获取锁操作会被加入到阻塞队列, 之后其他操作释放锁, 本线程获取到锁后, 会收到响应
            if (responseFuture.get(duration) == null) {
                // 没收到响应, 获取锁超时; 取消这个锁的等待
                unlock(request);
                return false;
            }

            // 获取锁成功
            log.info("get lock request:{}", request);
            // 需要操作之后移除锁, 将请求放到threadLocal中
            threadLocal.set(request);
            return true;

        } catch (InterruptedException e) {
            // 等待锁时被中断, 当成获取锁失败
            unlock(request);
            log.error("lock resume error", e);
            return false;
        }
    }

    /**
     * 发送解锁信息
     * 
     * @param request
     * @date: 2020年3月26日 上午10:45:25
     */
    private void unlock(Request request) {
        request.setOperate(Steps.Unlock);
        log.info("unlock request:{}", request);
        client.sendRequestWithoutResponse(request);
    }

    @Override
    public void lock(String resource) {
        tryLock(resource, Duration.ofSeconds(Integer.MAX_VALUE));
    }


    @Override
    public void unlock() {
        try {
            Request request = threadLocal.get();
            if (request == null) {
                log.debug("is repeat unlock");
                return;
            }
            unlock(request);
        } finally {
            threadLocal.remove();
        }

    }

    @Override
    public void shutdown() {
        client.shutdown();
        initProcessLatch.countDown();
    }

    /**
     * 修订进程ID
     * 
     * @param serverProcessId
     * @date: 2020年3月26日 上午10:12:25
     */
    public void revisionProcessId(long serverProcessId) {
        this.processId = serverProcessId;
        initProcessLatch.countDown();
    }

    /**
     * 服务退出, 重置信息
     * 
     * @date: 2020年3月26日 上午10:12:43
     */
    public void serverBreak() {
        this.processId = 0;
        this.initProcessLatch = new CountDownLatch(1);
    }

    /**
     * 同步的获取进程ID(等待连接成功)
     * 
     * @return long
     * @date: 2020年3月26日 上午10:07:19
     */
    private long syncGetProcessId() {
        try {
            if (this.processId == 0) {
                log.debug("waiting for server response process id");
                initProcessLatch.await();
            }
        } catch (Exception e) {
            log.error("waiting for process  is interrupted", e);
            throw new RuntimeException(e);
        }
        return this.processId;
    }

    /**
     * 原子递增, 创建lockId
     * 
     * @return long
     * @date: 2020年3月26日 上午10:22:21
     */
    private long createLockId() {
        return lockIdAdder.getAndIncrement();
    }

}



