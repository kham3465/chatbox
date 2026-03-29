package com.vn.nhom2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfig {
    @Bean
    public ThreadPoolExecutor executor() {
        int corePoolSize = 10;
        int maxPoolSize = 15;
        long keepAliveTime = 120L;
        TimeUnit unit = TimeUnit.SECONDS;
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, new ArrayBlockingQueue<>(100));
    }
}
