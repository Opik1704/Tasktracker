package com.site.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Value("${app.async.core-pool-size:0}")
    private int customCorePoolSize;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        int availableCores = Runtime.getRuntime().availableProcessors();

        int corePoolSize = (customCorePoolSize > 0) ? customCorePoolSize : availableCores * 2;
        int maxPoolSize = corePoolSize * 2;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("AsyncEvt-");


        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Критическая ошибка в асинхронном методе [{}]: {}",
                    method.getName(), throwable.getMessage(), throwable);
        };
    }
}
