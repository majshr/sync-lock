package com.model;

import java.io.Serializable;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月24日 上午10:03:40
 */
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 操作 */
    private Steps operate;

    /** 请求ID */
    private long lockId;

    /** 进程ID */
    private long processId;

    /** 锁资源字符串 */
    private String resource;

    public Request(long processId, long lockId, Steps operate, String resource) {
        this.processId = processId;
        this.lockId = lockId;
        this.operate = operate;
        this.resource = resource;
    }

    public Steps getOperate() {
        return operate;
    }

    public void setOperate(Steps operate) {
        this.operate = operate;
    }

    public long getLockId() {
        return lockId;
    }

    public void setLockId(long lockId) {
        this.lockId = lockId;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

}
