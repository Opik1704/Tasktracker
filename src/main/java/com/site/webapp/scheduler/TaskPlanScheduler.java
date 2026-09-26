package com.site.webapp.scheduler;

import com.site.webapp.service.TaskPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskPlanScheduler {
    private static final Logger log = LoggerFactory.getLogger(TaskPlanScheduler.class);

    private final TaskPlanService taskPlanService;

    public TaskPlanScheduler(TaskPlanService taskPlanService) {
        this.taskPlanService = taskPlanService;
    }

    @Scheduled(cron = "${app.scheduling.cron.weekly-plan:0 0 9 * * MON}")
    public void generateWeeklyPlans() {
        log.info("Запуск планировщика: еженедельный план задач");
        int usersCount = taskPlanService.sendWeeklyPlan();
        log.info("Еженедельный план отправлен пользователям: {}", usersCount);
    }

    @Scheduled(cron = "${app.scheduling.cron.daily-plan:0 0 9 * * *}")
    public void generateDailyPlans() {
        log.info("Запуск планировщика: ежедневный план задач");
        int usersCount = taskPlanService.sendDailyPlan();
        log.info("Ежедневный план отправлен пользователям: {}", usersCount);
    }

}
