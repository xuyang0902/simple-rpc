package com.xprc.commons.model;


import com.xprc.commons.exception.RpcException;

import java.util.concurrent.TimeUnit;

/**
 * 自定义furue
 * @author xu.qiang
 * @date 17/8/10
 */
public interface Future<T> {


    /**
     * 是否已经完成
     * @return
     */
    boolean isDone();

    /**
     * 等待 设计上这步应该阻塞直到获取结果 子类可以个性化实现
     * @return
     */
    T get();

    /**
     * 在规定时间内获取任务，如果任务超时 则抛异常
     * @param timeout
     * @param unit
     * @return
     */
    T get(long timeout, TimeUnit unit) throws RpcException;
}
