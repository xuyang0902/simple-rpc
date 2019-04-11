package com.xrpc.quick.starter.spring;

import org.springframework.stereotype.Service;

/**
 * @author xu.qiang
 * @date 17/8/19
 */
@Service
public class HelloImpl implements Hello {

    @Override
    public String tell(String text) {

        System.out.println("server revice :" + text);
        return "pong message";
    }
}
