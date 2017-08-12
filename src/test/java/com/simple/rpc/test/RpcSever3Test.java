package com.simple.rpc.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author xu.qiang
 * @date 17/8/10
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/server3.xml")
public class RpcSever3Test {


    @Test
    public  void hehe() throws InterruptedException {


        while(true){
            Thread.sleep(1000L);
        }

    }
}
