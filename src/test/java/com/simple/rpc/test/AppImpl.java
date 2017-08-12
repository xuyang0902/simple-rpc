package com.simple.rpc.test;

import com.simple.rpc.server.RpcService;
import org.springframework.stereotype.Service;

/**
 * @author xu.qiang
 * @date 17/8/10
 */
@RpcService(value = App.class ,version = "1.0")
@Service
public class AppImpl implements App {

    @Override
    public String hell(String hello) {
        System.out.println("!!!!!!!!!!" + hello);
        return "hello world";
    }
}
