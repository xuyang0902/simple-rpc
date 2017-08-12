package com.simple.rpc.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * rpc服务注解
 * @author xu.qiang
 * @date 17/8/11
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {

    /**
     * 接口名称
     */
    Class<?> value();

    /**
     * 版本号
     */
    String version();
}
