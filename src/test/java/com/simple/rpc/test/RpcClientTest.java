package com.simple.rpc.test;

import com.simple.BootClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author xu.qiang
 * @date 17/8/10
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/client.xml")
public class RpcClientTest {

    @Test
    public  void test() {

        long begin = System.currentTimeMillis();

        for(int i = 0 ; i < 100000;i++){
            App app = BootClient.create(App.class,"1.0");
            String hello = app.hell("hello");

            System.out.println(hello);
        }

        long end = System.currentTimeMillis();

        System.out.println((end-begin));
    }
}
