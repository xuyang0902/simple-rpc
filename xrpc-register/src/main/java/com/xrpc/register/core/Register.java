package com.xrpc.register.core;


import com.xprc.commons.model.RpcURL;

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
     * 服务端 停机，重启 取消注册   todo
     * <p>
     * @param rpcURL
     */
    void doUnregister(RpcURL rpcURL);

    /**
     * 客户端订阅
     * @param rpcURL
     */
    void doSubscribe(RpcURL rpcURL);

    /**
     * 客户端取消订阅了  todo
     * <p>
     * @param rpcURL
     */
    void doUnSubscribe(RpcURL rpcURL);
}
