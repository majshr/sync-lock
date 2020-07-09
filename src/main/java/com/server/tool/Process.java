package com.server.tool;

import io.netty.channel.Channel;

/**
 * 进程(管理进程所有操作, 进程channel等信息)
 */
public class Process {
    /** 进程唯一ID, 服务端维护, 每个连接到服务端的客户端对应一个ID */
    private long processId;

    /** 进程的channel */
    private Channel channel;


    public Process(Channel channel) {
        this.channel = channel;
        this.processId = ProcessCarrier.getInstance().generateProcessId();
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

}
