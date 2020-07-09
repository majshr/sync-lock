package com.model;

import java.io.Serializable;

/**
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月25日 上午11:24:35
 */
public class Response implements Serializable {

    private static final long serialVersionUID = 4585603043346005333L;
    private Steps operate;
    /**
     * 也就是请求ID
     */
    private long lockId;
    private ResponseCode code;
    /**
     * 进程ID
     */
    private long processId;
    /**
     * 资源, 锁对应的字符串
     */
    private String resource;

    /**
     * 信息
     */
    private String message;

    public Response(Steps operate, long processId, long lockId, String resource, ResponseCode code) {
        this.operate = operate;
        this.processId = processId;
        this.lockId = lockId;
        this.code = code;
        this.resource = resource;
    }

    public Response() {
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

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
