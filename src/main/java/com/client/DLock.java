package com.client;

import java.time.Duration;

/**
 * 
 * @author mengaijun
 * @Description: 锁操作接口定义
 * @date: 2020年3月25日 下午5:17:19
 */
public interface DLock {

    /**
     * 尝试获取锁，立即返回是否获取成功
     * 
     * @param resource
     * @return boolean
     */
    boolean tryLock(String resource);

    /**
     * 对指定资源加锁,如果抢占不到锁会阻塞
     * @param resource
     */
    void lock(String resource);

    /**
     * 额外设置获取锁超时时间
     * @param resource
     * @param duration
     * @return TODO
     */
    boolean tryLock(String resource, Duration duration);

    /**
     * 解锁操作
     */
    void unlock();

    /**
     * 关机,关闭连接资源
     */
    void shutdown();
}
