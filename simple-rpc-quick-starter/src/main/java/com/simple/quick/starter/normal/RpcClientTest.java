package com.simple.quick.starter.normal;

import com.simple.core.BootClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author xu.qiang
 * @date 17/8/10
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/client.xml")
public class RpcClientTest {

    @Test
    public void test() throws InterruptedException, ExecutionException {


        ExecutorService executorService = Executors.newFixedThreadPool(16);

        List<Future<String>> list = new ArrayList<Future<String>>();
        long begin = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {

            final int finalI = i;
            Future<String> hello = executorService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    App app = BootClient.create(App.class, "1.0");
                    String hello = app.hello("hello_" + finalI);
                    return hello;
                }
            });

            list.add(hello);

        }

        for (Future<String> stringFuture : list) {
            System.out.println(stringFuture.get());
        }

        long end = System.currentTimeMillis();

        System.out.println((end - begin) );


        while (true) {
            Thread.sleep(1000L);
        }


    }
}
