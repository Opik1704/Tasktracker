package com.site.webapp.service;

import com.site.webapp.exception.UserNotFoundException;
import com.site.webapp.models.Notification;
import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.repo.NotificationRepository;
import com.site.webapp.repo.UserRepository;
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
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository){
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
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

    public void markAllAsRead(Long userId) {
        if (userId == null) {
            log.warn("Попытка отметить сообщения прочитанными для null userId");
            return;
        }

        List<Notification> unread = notificationRepository.findAllByUserIdAndReadFalse(userId);

        if(unread.isEmpty()){
            return;
        }
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);

        log.info("Пользователь ID {} отметил сообщения как прочитанные", userId);
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
