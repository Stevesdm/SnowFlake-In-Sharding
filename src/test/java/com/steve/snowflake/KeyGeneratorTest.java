package com.steve.snowflake;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @Description:
 * @Author: stevejobson
 * @CreateDate: 2018/1/6 下午3:45
 */

public class KeyGeneratorTest {

    @Test
    public void testKeyGenerator() throws Exception{
        int threadNumber = Runtime.getRuntime().availableProcessors() << 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadNumber);
        final int taskNumber = threadNumber << 2;
        final KeyGenerator keyGenerator = new KeyGenerator();
        Set<Number> generatedKeys = new HashSet<Number>();
        for (int i = 0; i < taskNumber; i++) {
            Number number = executor.submit(new Callable<Number>() {

                public Number call() throws Exception {
                    return keyGenerator.generateKey();
                }
            }).get();

            System.out.println(number.longValue());
            generatedKeys.add(number);
        }
        assertThat(generatedKeys.size(), is(taskNumber));
    }
}
