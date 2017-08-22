package com.simple.quick.starter.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author xu.qiang
 * @date 17/8/10
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/s02.xml")
public class RpcSeverTest2 {

    private Logger logger  = LoggerFactory.getLogger(RpcSeverTest2.class);

    @Test
    public  void hehe() throws InterruptedException {

        logger.info("start----servr -02");
        while(true){
            Thread.sleep(1000L);
        }

    }
}
