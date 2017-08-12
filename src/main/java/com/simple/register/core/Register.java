package com.simple.register.core;

import com.simple.rpc.model.RpcURL;

/**
 * 注册中心
 *
 * @author xu.qiang
 * @date 17/8/10
 */
interface Register {


    /**
     * 服务端注册
     * @param rpcURL
     */
    void doRegister(RpcURL rpcURL);

    /**
     * 服务端 停机，重启 取消注册
     * <p>
     * todo 服务进程被杀死，这个时候程序异常退出。。。 如何处理？？？
     * @param rpcURL
     */
    void doUnregister(RpcURL rpcURL);

    /**
     * 客户端订阅
     * @param rpcURL
     */
    void doSubscribe(RpcURL rpcURL);

    /**
     * 客户端取消订阅了
     * <p>
     * todo 服务进程被杀死，这个时候程序异常退出。。。 如何处理？？？
     * @param rpcURL
     */
    void doUnSubscribe(RpcURL rpcURL);
}
