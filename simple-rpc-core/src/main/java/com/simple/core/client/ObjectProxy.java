package com.simple.core.client;


import com.simple.commons.exception.RpcException;
import com.simple.commons.model.RpcResponse;
import com.simple.commons.model.RpcURL;
import com.simple.core.BootClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author xu.qiang
 * @date 17/8/11
 */
public class ObjectProxy<T> implements InvocationHandler {


    private Class<T> clazz;
    private String version;


    public ObjectProxy(Class<T> clazz, String version) {
        this.clazz = clazz;
        this.version = version;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcURL rpcURL = new RpcURL();
        rpcURL.setServiceBean(method.getDeclaringClass().getName());
        rpcURL.setMethodName(method.getName());
        rpcURL.setParameterTypes(method.getParameterTypes());
        rpcURL.setParameters(args);
        rpcURL.setVersion(version);

        //默认6秒超时时间
        RpcResponse rpcResponse = BootClient.getNettyRpcCilent().invokeSync(rpcURL, 60000L);

        if (!rpcResponse.isError()) {
            return rpcResponse.getResult();
        }

        throw new RpcException(rpcResponse.getError());
    }
}
