package com.site.webapp.job;

import com.site.webapp.service.InviteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InviteCleanupJob {
    private static final Logger log = LoggerFactory.getLogger(InviteCleanupJob.class);

    private final InviteService inviteService;

    public InviteCleanupJob(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @Scheduled(cron = "${app.scheduling.cron.cleanup-invites:0 0 2 * * *}")
    public void deleteExpiredInvites() {
        log.info("Запуск фоновой задачи: очистка просроченных инвайтов");

        int countDeletedInvites = inviteService.deleteExpiredInvites();

        log.info("Очистка инвайтов: удалено {} просроченных токенов", countDeletedInvites);
    }
}
