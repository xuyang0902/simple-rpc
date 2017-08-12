package com.simple;

/**
 *  远程服务
 *
 *  @author xu.qiang
 * @date 17/8/10
 */
public interface RemotingService {

    /**
     * 启动服务
     */
    void start();


    /**
     * 关闭服务
     */
   void shutdown();


}
