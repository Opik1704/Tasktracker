package com.site.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

/**
 * Конфигурация планировщика задач.
 * Настраивает ThreadPoolTaskScheduler для @Scheduled методов:
 * - Размер пула: 5 потоков
 * - Обработка ошибок в фоновых задачах
 */

@Component
public class SchedulerConfig implements SchedulingConfigurer {
    private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("scheduled-task-");

        scheduler.setErrorHandler(throwable ->
                log.error("Сбой при выполнении фоновой задачи: {}", throwable.getMessage(), throwable)
        );

        scheduler.initialize();
        taskRegistrar.setTaskScheduler(scheduler);
    }
}
