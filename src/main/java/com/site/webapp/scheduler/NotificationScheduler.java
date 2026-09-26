package com.site.webapp.scheduler;

import com.site.webapp.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Планировщик для работы с уведомлениями.
 * Запускается по расписанию для:
 * - Отправки срочных напоминаний (каждый час)
 * - Очистки старых уведомлений (каждый день в 2:00)
 */
@Component
public class NotificationScheduler {
    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationService notificationService;

    public NotificationScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${app.scheduling.cron.urgent-reminders:0 0 * * * *}")
    public void processUrgentReminders() {
        log.info("Запуск планировщика: обработка срочных напоминаний");
        int count = notificationService.sendUrgentReminders();
        log.info("Обработка срочных напоминаний завершена. Отправлено: {}", count);
    }


    @Scheduled(cron = "${app.scheduling.cron.cleanup-notifications:0 0 2 * * *}")
    public void cleanupOldNotifications() {
        log.info("Запуск планировщика: очистка старых уведомлений");
        long count = notificationService.deleteOldNotifications();
        log.info("Очистка старых уведомлений завершена. Удалено: {}", count);
    }

}
