package com.site.webapp.listeners;

import com.site.webapp.events.TaskCreatedEvent;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationEventListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public NotificationEventListener(UserRepository userRepository,NotificationService notificationService){
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

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
}
