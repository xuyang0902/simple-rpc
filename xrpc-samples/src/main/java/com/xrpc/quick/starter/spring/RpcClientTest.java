package com.xrpc.quick.starter.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;

/**
 * @author xu.qiang
 * @date 17/8/10
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/c01.xml")
public class RpcClientTest {


    private Logger logger = LoggerFactory.getLogger(RpcClientTest.class);
    @Autowired
    private Hello hello;


    @Test
    public void test() throws InterruptedException, ExecutionException {
        logger.info("start----client");

        for (int i = 0; i < 100; i++) {
            String hello = this.hello.tell("hello" + i);
            System.out.println(hello);

        }

        Thread.sleep(1000*60);


    }
}
