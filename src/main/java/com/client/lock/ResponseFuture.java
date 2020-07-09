package com.client.lock;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.model.Request;
import com.model.Response;

/**
 * 一个异步回调的操作
 * 
 * @author mengaijun
 * @Description: TODO
 * @date: 2020年3月25日 下午5:38:08
 */
public class ResponseFuture {
    static Logger log = LoggerFactory.getLogger(ResponseFuture.class);

    private final Request request;
    private Response response;

    private final CountDownLatch latch = new CountDownLatch(1);

    public ResponseFuture(Request request) {
        this.request = request;
    }

    /**
     * 阻塞获取结果
     * 
     * @return Response
     * @throws InterruptedException
     * @date: 2020年3月25日 下午5:43:33
     */
    public Response get() throws InterruptedException {
        latch.await();
        return response;
    }

    /**
     * 获取结果, 超时返回null
     * 
     * @param duration
     * @return Response
     * @throws InterruptedException
     * @date: 2020年3月25日 下午5:44:08
     */
    public Response get(Duration duration) throws InterruptedException {
        boolean await = latch.await(duration.toNanos(), TimeUnit.NANOSECONDS);
        // 响应超时
        if (await == false) {
            String msg = "wait lock:[" + request + "] timeout:" + duration.getSeconds() + "s";
            log.error(msg);
            return null;
        }

        // 正常响应
        return response;
    }

    /**
     * 设置响应结果, 唤醒等待结果的线程
     * 
     * @param response
     * @date: 2020年3月25日 下午5:47:25
     */
    public void setResponse(Response response) {
        this.response = response;
        latch.countDown();
    }

    /**
     * 设置响应结果错误, 没有结果; 唤醒等待结果的线程
     * 
     * @date: 2020年3月26日 上午10:29:44
     */
    public void inactive() {
        response = null;
        latch.countDown();
    }

}
