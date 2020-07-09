package com.client.test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.client.Config;
import com.client.DLock;
import com.client.DLockTool;

public class Test {
    public static void main(String[] args) {
        Config config = Config.config();
        config.host("localhost").port(8899);

        DLockTool dSync = DLockTool.create(config);
        DLock dLock = dSync.getLock();

        if (dLock.tryLock("abc", Duration.ofSeconds(3))) {
            try {
                System.out.println("我获取到锁了!");

                TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                dLock.unlock();
            }
        } else {
            System.out.println("没获取到锁");
        }
    }

}
