package com.simple.core.client;

import com.simple.commons.model.RpcResponse;
import com.simple.commons.model.RpcURL;

/**
 * 客户端接口
 *
 * @author xu.qiang
 * @date 17/8/10
 */
public interface RpcClient {


    /**
     * 同步调用
     *
     * @param url
     * @param timeoutMillis
     * @return
     */
    RpcResponse invokeSync(final RpcURL url, final long timeoutMillis);


    // TODO: 17/8/10   异步调用  点对点单向调用


}
