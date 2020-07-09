package com.server.tool;

import java.util.Objects;

import io.netty.channel.Channel;

/**
 * 操作信息
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月26日 上午11:38:27
 */
public class Operate {
    private String operateId;
    private long lockId;
    private Channel channel;

    /** 进程ID */
    private long processId;

    /** 资源(多个线程要抢占的的资源) */
    private String resource;

    /** 操作是否激活 */
    private volatile boolean active = true;

    public Operate(long processId, long lockId, String resource, Channel channel) {
        this.processId = processId;
        this.lockId = lockId;
        this.operateId = processId + "-" + lockId;
        this.resource = resource;
        this.channel = channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Operate operate = (Operate) o;
        return Objects.equals(lockId, operate.lockId) && Objects.equals(operateId, operate.operateId)
                && Objects.equals(processId, operate.processId) && Objects.equals(resource, operate.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operateId, lockId, processId, resource);
    }

    @Override
    public String toString() {
        return "Operate [operateId=" + operateId + ", lockId=" + lockId + ", channel=" + channel + ", processId="
                + processId + ", resource=" + resource + ", active=" + active + "]";
    }

    public void inactive() {
        this.active = false;
    }

    public String getOperateId() {
        return operateId;
    }

    public void setOperateId(String operateId) {
        this.operateId = operateId;
    }

    public long getLockId() {
        return lockId;
    }

    public void setLockId(long lockId) {
        this.lockId = lockId;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

}
