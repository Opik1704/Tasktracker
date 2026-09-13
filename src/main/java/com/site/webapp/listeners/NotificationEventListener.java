package com.site.webapp.listeners;

import com.site.webapp.events.FileAttachedEvent;
import com.site.webapp.events.TaskCreatedEvent;
import com.site.webapp.events.TaskDeletedEvent;
import com.site.webapp.events.TaskUpdatedEvent;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

@Component
public class NotificationEventListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public NotificationEventListener(UserRepository userRepository,NotificationService notificationService){
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreatedEvent(TaskCreatedEvent event) {
        log.debug("Task created event received: {}", event);
        if (event.artistId() == null) {
            return;
        }

        String message = String.format("%s назначил(а) вам задачу '%s' с дедлайном %s и приоритетом %s",
                event.creatorFullName(), event.taskTitle(), event.deadline(), event.priority());

        notificationService.createNotification(event.artistId(), event.taskId(), message);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskUpdatedEvent(TaskUpdatedEvent event){
        log.debug("Task updated event received: {}", event);

        if (!Objects.equals(event.oldArtistId(), event.newArtistId())) {
            if (event.oldArtistId() != null) {
                String msg = String.format("%s передал(а) вашу задачу '%s'",
                        event.initiatorName(), event.taskTitle());
                notificationService.createNotification(event.oldArtistId(), event.taskId(), msg);
            }
            if (event.newArtistId() != null) {
                String msg = String.format("%s назначил(а) вам задачу '%s'",
                        event.initiatorName(), event.taskTitle());
                notificationService.createNotification(event.newArtistId(), event.taskId(), msg);
            }
            return;
        }

        if (event.newArtistId() != null && !Objects.equals(event.oldDeadline(), event.newDeadline())) {
            String msg = String.format("%s изменил(а) дедлайн задачи '%s' на %s",
                    event.initiatorName(), event.taskTitle(), event.newDeadline());
            notificationService.createNotification(event.newArtistId(), event.taskId(), msg);
        }

        if (event.newArtistId() != null && !Objects.equals(event.oldPriority(), event.newPriority())) {
            String msg = String.format("%s изменил(а) приоритет задачи '%s' на %s",
                    event.initiatorName(), event.taskTitle(), event.newPriority());
            notificationService.createNotification(event.newArtistId(), event.taskId(), msg);
        }


    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFileAttachedEvent(FileAttachedEvent event){
        log.info("Обработка прикрепления файла '{}' к задаче ID {}", event.fileName(), event.taskId());

        String message = "К вашей задаче прикреплен новый файл: " + event.fileName();
        notificationService.createNotification(event.artistId(), event.taskId(), message);

    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskDeletedEvent(TaskDeletedEvent event){
        log.debug("Task deleted event received: {}", event);

        notificationService.deleteAllByTaskId(event.taskId());

        if (event.artistId() != null){
            String msg = event.deleterEmail() + " удалил задачу '" + event.taskTitle() + "', которая была назначена вам";
            notificationService.createNotification(event.artistId(), msg);
        }

    }
}
