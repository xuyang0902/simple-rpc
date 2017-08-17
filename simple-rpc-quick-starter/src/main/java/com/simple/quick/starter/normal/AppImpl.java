package com.simple.quick.starter.normal;

import com.simple.core.server.RpcService;
import org.springframework.stereotype.Service;

/**
 * @author xu.qiang
 * @date 17/8/10
 */
@RpcService(value = App.class, version = "1.0")
@Service
public class AppImpl implements App {

    @Override
    public String hello(String hello) {
        System.out.println(hello);
        return String.format("server_pong_msg:%s", hello);
    }
}
