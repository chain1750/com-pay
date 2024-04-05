package com.chaincat.pay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

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
        return createExecutor("notify-executor-");
    }

    /**
     * 定时任务线程池
     *
     * @return Executor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return createExecutor("task-executor-");
    }

    /**
     * 创建线程池
     *
     * @param prefix 前缀
     * @return Executor
     */
    private Executor createExecutor(String prefix) {
        int processNum = Runtime.getRuntime().availableProcessors();
        int corePoolSize = (int) (processNum / (1 - 0.2));
        int maxPoolSize = (int) (processNum / (1 - 0.5));

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(maxPoolSize * 1000);
        executor.setThreadPriority(Thread.MAX_PRIORITY);
        executor.setDaemon(false);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix(prefix);
        return executor;
    }
}
