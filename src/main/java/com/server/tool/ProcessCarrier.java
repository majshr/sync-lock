package com.server.tool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

/**
 * 进程载体(process进程管理器)
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月24日 下午2:27:10
 */
public class ProcessCarrier {
    static Logger log = LoggerFactory.getLogger(ProcessCarrier.class);

    /**
     * "进程ID:进程对象"的map
     */
    private Map<Long, Process> processIdMap = new ConcurrentHashMap<>();
    /**
     * "进程channelId:进程对象"的map
     */
    private Map<ChannelId, Process> processChannelMap = new ConcurrentHashMap<>();
    /**
     * 用于生成进程唯一ID(服务端维护进程ID, 确保唯一性)
     */
    private AtomicInteger processIdAdder = new AtomicInteger(1);

    private static ProcessCarrier instance = new ProcessCarrier();

    private ProcessCarrier() {

    }

    /**
     * 单例获取
     * 
     * @return ProcessCarrier
     * @date: 2020年3月27日 下午3:34:58
     */
    public static ProcessCarrier getInstance() {
        return instance;
    }

    /**
     * 添加进程
     * 
     * @param process
     * @date: 2020年3月24日 下午2:34:48
     */
    public void addProcess(Process process) {
        processIdMap.put(process.getProcessId(), process);
        processChannelMap.put(process.getChannel().id(), process);
    }

    /**
     * 生成进程ID
     * 
     * @return long
     * @date: 2020年3月24日 下午2:39:39
     */
    public long generateProcessId() {
        return processIdAdder.getAndIncrement();
    }

    /**
     * 根据进程ID获取进程
     * 
     * @param processId
     * @return Process
     * @date: 2020年3月24日 下午2:39:48
     */
    public Process get(long processId) {
        return processIdMap.get(processId);
    }

    /**
     * 根据channel信息获取进程
     * 
     * @param channel
     * @return Process
     * @date: 2020年3月24日 下午2:40:09
     */
    public Process get(Channel channel) {
        return processChannelMap.get(channel.id());
    }

    /**
     * 添加操作
     * 
     * @param operate
     * @date: 2020年3月24日 下午3:04:12
     */
    // public void addProcessWaitingOperate(Operate operate) {
    // log.debug("process register operate:{}", operate);
    // if (operate != null) {
    // Process process = processIdMap.get(operate.getProcessId());
    // if (process != null) {
    // process.addWaitingOperates(operate);
    // }
    // }
    // }

    /**
     * 移除操作
     * 
     * @param operate
     * @date: 2020年3月24日 下午3:04:25
     */
    // public void removeProcessWaitingOperate(Operate operate) {
    // log.debug("process remove operate:{}", operate);
    // if (operate != null) {
    // Process process = processIdMap.get(operate.getProcessId());
    // if (process != null) {
    // process.removeWaitingOperates(operate);
    // }
    // }
    // }

    /**
     * 移除进程
     * 
     * @param process
     * @date: 2020年3月24日 下午3:05:18
     */
    public void removeProcess(Process process) {
        processIdMap.remove(process.getProcessId());
        processChannelMap.remove(process.getChannel().id());
    }

}
