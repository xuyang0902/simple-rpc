package com.simple;

import com.simple.rpc.client.NettyRpcCilent;
import com.simple.rpc.client.ObjectProxy;

import java.lang.reflect.Proxy;

/**
 * 客户端启动管理
 *
 * @author xu.qiang
 * @date 17/8/11
 */
public class BootClient {


    private static NettyRpcCilent nettyRpcCilent = new NettyRpcCilent();

    static {
        nettyRpcCilent.start();
    }

    public static NettyRpcCilent getNettyRpcCilent() {
        return nettyRpcCilent;
    }


    /**
     * 动态创建 interfaceClass的代理对象
     * @param interfaceClass
     * @param version
     * @param <T>
     * @return
     */
    public static <T> T create(Class<T> interfaceClass,String version) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<T>(interfaceClass,version)
        );
    }
}
