package com.chaincat.pay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池工厂
 *
 * @author chenhaizhuang
 */
@Configuration
public class ExecutorFactoryConfig {

    /**
     * 回调通知线程池
     *
     * @return Executor
     */
    @Bean(name = "notifyExecutor")
    public Executor notifyExecutor() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("notify-executor-");
        // 如果同时处理的通知太多，则直接报错
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }

    /**
     * 定时任务线程池
     *
     * @return Executor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("task-executor-");
        // 定时任务如果超过线程池任务数量则直接拒绝，由下次定时触发再执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }
}
