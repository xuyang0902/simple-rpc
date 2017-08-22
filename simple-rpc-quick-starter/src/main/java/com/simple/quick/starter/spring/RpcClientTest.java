package com.simple.quick.starter.spring;

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
    private App2 app2;

    @Autowired
    private App3 app3;

    @Test
    public void test() throws InterruptedException, ExecutionException {
        logger.info("start----client");

        for (int i = 0; i < 1; i++) {
            String hello = app2.tell("hello" + i);
            System.out.println(hello);

            String hello2 = app3.tell("hello" + i);
            System.out.println("222---" + hello2);

        }


    }
}
