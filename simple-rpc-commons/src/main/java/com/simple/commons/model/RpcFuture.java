package com.simple.commons.model;


import com.simple.commons.exception.RpcException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * rpc future
 *
 * @author xu.qiang
 * @date 17/8/9
 */
public class RpcFuture implements Future<RpcResponse> {

    private RpcURL request;

    private RpcResponse response;

    private int timeout;

    private final Lock lock = new ReentrantLock();

    private final Condition done = lock.newCondition();

    public RpcFuture(RpcURL request) {
        this.request = request;
    }

    @Override
    public boolean isDone() {
        /**
         * 响应信息存在 == 完成
         */
        return response != null;
    }

    @Override
    public RpcResponse get() {

        if (isDone()) {
            return response;
        }

        return get(timeout, TimeUnit.MILLISECONDS);

    }


    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws RpcException {
        if (timeout <= 0) {
            timeout = 1000;
        }

        long start = System.currentTimeMillis();

        lock.lock();

        try {

            while (!isDone()) {
                //阻塞 等待
                done.await(timeout, TimeUnit.MILLISECONDS);
                if (isDone() || timeout < System.currentTimeMillis() - start) {
                    break;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

        //到这里还没有拿到响应结果  肯定是因为超时了
        if (!isDone()) {
            throw new RpcException("time out");
        }

        return response;
    }

    /**
     * 接受到信息
     *
     * @param response
     */
    public void recieve(RpcResponse response) {

        if (response == null) {
            throw new RpcException("response can not be null");
        }
        lock.lock();

        try{
            if (isDone()) {
                return;
            }

            this.response = response;

            //通知 可以获取结果了
            if (done != null) {
                done.signal();
            }
        }finally {
            lock.unlock();
        }


    }

}
