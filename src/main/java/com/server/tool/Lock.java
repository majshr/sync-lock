package com.server.tool;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 维护锁对应的操作信息(每一个资源resource对应一个Lock对象, 对应多个操作)
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月26日 上午11:04:47
 */
public class Lock {
    static Logger log = LoggerFactory.getLogger(Lock.class);

    /**
     * 得到资源的当前操作(设置为当前操作成功的操作, 当成获取资源成功)
     */
    private volatile Operate currentOperate = null;
    private static AtomicReferenceFieldUpdater<Lock, Operate> curOperateUpdater = AtomicReferenceFieldUpdater
            .newUpdater(Lock.class, Operate.class, "currentOperate");
    /**
     * 等待资源的操作队列
     */
    private Queue<Operate> queueWaiter = new LinkedList<>();

    /**
     * 获取资源操作, 获取失败, 加入操作等待队列
     * 
     * @param operate
     * @return boolean
     * @date: 2020年3月24日 上午11:34:41
     */
    public synchronized boolean acquire(Operate operate) {
        // *********2, 为什么此处需要同步?
        // 线程1获取锁成功, 线程2获取锁失败, 进入阻塞队列; 此时线程1释放锁, 从阻塞队列中查找下一个操作, 发生在操作2
        // 进入阻塞队列之前, 没有唤醒信息, 线程2一直阻塞了
        if (curOperateUpdater.compareAndSet(this, null, operate)) {
            log.info("获取锁成功");
            return true;
        } else {
            log.info("获取锁失败, 进入阻塞队列");
            // **********1, 为什么这里需要同步?
            // 线程1获取锁成功, 线程2获取锁失败, 进入阻塞队列; 如果此时线程1释放锁, 从阻塞队列中查找下一个操作,
            // 但线程2操作还没加入到队列, 线程1遍历队列, 没有发现下一个操作; 之后线程2将操作加入到队列
            // 就会导致线程2获取不到锁, 而一直阻塞了
            queueWaiter.add(operate);

            return false;
        }
    }

    /**
     * 尝试获取资源, 获取失败, 不加到等待队列
     * 
     * @param operate
     * @return boolean
     * @date: 2020年3月24日 上午11:35:23
     */
    public synchronized boolean tryAcquire(Operate operate) {
        // 修改成功的一个, 为获得锁成功的线程
        return curOperateUpdater.compareAndSet(this, null, operate);
    }

    /**
     * 释放活跃的操作<br>
     * 1,可能释放的是当前操作; 释放并返回下一个争抢锁 <br>
     * 2,可能释放的是等待锁超时的操作, 将超时的操作从队列中移除<br>
     *      超时的锁也可能超时之后被唤醒了, 然后释放, 相当于释放当前锁<br>
     *      超时的锁, 超时了, 没有唤醒, 释放队列中等待的锁<br>
     * 3, 释放不活跃的操作(如果某个client断开连接, 释放改client process的所有操作)<br>
     *      在查找下一个锁的时候, 如果channel断开, 会进行释放<br>
     * 
     * @param operate
     * @return Operate
     * @date: 2020年3月27日 下午2:39:19
     */
    public synchronized Operate release(Operate operate) {
        log.info("释放锁");
        // 释放的操作为空, 错误
        if (operate == null) {
            log.error("error of operate");
            return null;
        }

        // operate不为当前操作
        if (!Objects.equals(currentOperate, operate)) {
            // 等待锁超时的操作, 将等待操作从队列中移除
            queueWaiter.remove(operate);
            return null;
        }

        // **********释放当前操作
        // 当前操作不存在, 错误
        if (currentOperate == null) {
            log.error("释放锁错误, 当前获取锁的操作为null");
            return null;
        }

        // 当前操作为需要释放的操作, 释放
        return updateNextOperateFromQueue();
    }

    /**
     * 从等待队列中更新下一个获取到资源的操作
     * 
     * @return Operate
     * @date: 2020年3月27日 下午2:56:57
     */
    private Operate updateNextOperateFromQueue() {
        // 先设置当前操作为null
        currentOperate = null;

        // 此时可能新线程抢占资源, 也可能从队列中取出一个操作, 抢占资源
        Operate nextOperate = queueWaiter.peek();
        // 如果操作不为空, 但没有激活, 向下查找查找下一个操作
        while (nextOperate != null && (!nextOperate.isActive() || !nextOperate.getChannel().isActive())) {
            queueWaiter.poll();
            nextOperate = queueWaiter.peek();
        }

        if (nextOperate == null) {
            return null;
        }

        if (curOperateUpdater.compareAndSet(this, null, nextOperate)) {
            // 将队列中头元素操作设置为当前操作成功, 从队列中移除头操作
            return queueWaiter.poll();
        }

        // 队列中取出的头元素抢占资源失败, 被新线程请求获取到资源, 返回null
        return null;
    }

    /**
     * 获取当前操作
     * 
     * @return Operate
     * @date: 2020年3月24日 上午11:35:07
     */
    public Operate getCurrentOperate() {
        return currentOperate;
    }
}
