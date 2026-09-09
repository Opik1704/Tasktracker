package com.site.webapp.service;

import com.site.webapp.models.Notification;
import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.repo.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository){
        this.notificationRepository = notificationRepository;
    }


    public void send(User user, Task task, String message){
        if (user == null) {
            log.warn("Попытка отправить уведомление null пользователю");
            return;
        }
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setTask(task);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
        log.info("Пользователю {} отправлено сообщение {}", user.getEmail(),message);
    }
    public void send(User user, String message) {
        send(user, null, message);
    }

    public void markAllAsRead(User user) {
        if (user == null) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }

        List<Notification> unread = notificationRepository.findAllByUserIdAndReadFalse(user.getId());
        if(unread.isEmpty()){
            return;
        }
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        log.info("Пользователю {} отметил сообщения как прочитанные", user.getEmail());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
    public List<Notification> getLastNotifications(Long userId) {
        return notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }

    public void deleteAllByTaskId(Long taskId){
        notificationRepository.deleteAllByTaskId(taskId);
        log.info("Удалены все уведомления, связанные с задачей ID: {}", taskId);
    }

    public void deleteOldNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        long deleted = notificationRepository.deleteAllByCreatedAtBefore(threshold);
        log.info("Удалено {} старых уведомлений", deleted);
    }
}
