package com.client;

import com.client.lock.DLockImpl;

/**
 * 与server端连接信息; 获取lock对象
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月25日 下午5:25:00
 */
public class DLockTool {

    private final Config config;
    private volatile DLock lock;

    public DLockTool(Config config) {
        this.config = config;
    }

    /**
     * 创建DSync对象
     * 
     * @param config
     * @return DSync
     * @date: 2020年3月26日 上午10:59:00
     */
    public static DLockTool create(Config config) {
        return new DLockTool(config);
    }

    /**
     * 获取锁对象
     * 
     * @return DLock
     * @date: 2020年3月26日 上午10:59:31
     */
    public DLock getLock() {
        if (lock == null) {
            synchronized (this) {
                if (lock == null) {
                    DLockImpl dLock = new DLockImpl(config);
                    dLock.init();
                    lock = dLock;
                }
            }
        }
        return lock;
    }

}
