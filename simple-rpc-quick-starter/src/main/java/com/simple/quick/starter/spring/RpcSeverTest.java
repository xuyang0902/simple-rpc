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
@ContextConfiguration(locations = "classpath:/META-INF/spring/s01.xml")
public class RpcSeverTest {

    private Logger logger  = LoggerFactory.getLogger(RpcSeverTest.class);

    @Test
    public  void hehe() throws InterruptedException {

        logger.info("start----servr -01");
        while(true){
            Thread.sleep(1000L);
        }

    }
}
