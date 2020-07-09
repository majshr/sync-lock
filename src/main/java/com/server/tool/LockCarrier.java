package com.server.tool;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 锁载体(锁管理器)
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月24日 下午3:21:04
 */
public class LockCarrier {
    static Logger log = LoggerFactory.getLogger(LockCarrier.class);

    /**
     * 锁资源记录表(key:resource, value为Lock对象)
     */
    private ConcurrentHashMap<String, Lock> lockMap = new ConcurrentHashMap<>();

    /**
     * 进程载体
     */
    private ProcessCarrier processCarrier = ProcessCarrier.getInstance();

    /**
     * 单例
     */
    private static final LockCarrier lockCarrier = new LockCarrier();

    /**
     * 单例
     * 
     * @return LockCarrier
     * @date: 2020年7月7日 下午7:15:37
     */
    public static LockCarrier getInstance() {
        return lockCarrier;
    }

    /**
     * 尝试获取资源, 获取不到立即返回false, 且不加入等待队列
     * 
     * @return boolean
     * @date: 2020年3月24日 下午5:00:54
     */
    public boolean tryAcquire(Operate operate) {
        String resource = operate.getResource();

        // 获取资源
        Lock lock = getLockByResource(resource);

        return lock.tryAcquire(operate);
    }

    /**
     * 获取资源, 获取不到资源的操作加入到锁的等待队列
     * 
     * @return boolean
     * @date: 2020年3月24日 下午5:00:54
     */
    public boolean acquire(Operate operate) {
        String resource = operate.getResource();

        // 获取资源
        Lock lock = getLockByResource(resource);

        return lock.acquire(operate);
    }

    /**
     * 释放操作对应的锁, 返回下一个获取到锁的操作
     * 
     * @param operate
     * @return Operate
     * @date: 2020年3月25日 上午10:28:15
     */
    public Operate release(Operate operate) {
        String resource = operate.getResource();

        Lock lock = getLockByResource(resource);

        return lock.release(operate);
    }

    /**
     * 进程释放所有锁资源(释放进程)
     * 
     * @param process
     */
    public void processRelease(Process process) {
        log.debug("process release:{}", process);
        if (process == null) {
            return;
        }

        // 进程设置为非激活状态; 所有operate设置为非激活状态
        processCarrier.removeProcess(process);
    }

    /**
     * 返回不可变的LockMap(包含所有的Lock锁对象)
     * 
     * @return Map
     * @date: 2020年3月24日 下午3:46:00
     */
    public Map<String, Lock> peekLockMap() {
        return Collections.unmodifiableMap(lockMap);
    }

    /**
     * 获取资源对应的Lock(可能多线程并发访问, ConcurrentHashMap保证只有一个实例)
     * 
     * @param resource
     * @return Lock
     * @date: 2020年3月27日 下午4:57:46
     */
    private Lock getLockByResource(String resource) {
        // 获取资源
        Lock lock = lockMap.get(resource);
        if (lock == null) {
            Lock newLock = new Lock();
            // put成功, 返回null; put失败, 返回该位置的值
            lock = lockMap.putIfAbsent(resource, newLock);
            // put成功, map中的值为newLock; put失败, lock即为map中的值
            if (lock == null) {
                lock = newLock;
            }
        }

        return lock;
    }

}
