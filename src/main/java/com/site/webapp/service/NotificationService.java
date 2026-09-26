package com.site.webapp.service;

import com.site.webapp.exception.TaskNotFoundException;
import com.site.webapp.exception.UserNotFoundException;
import com.site.webapp.models.Notification;
import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.repo.NotificationRepository;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, TaskRepository taskRepository){
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public void createNotification(Long userId, Long taskId, String message) {
        if (userId == null) {
            log.warn("Попытка создать уведомление для null userId");
            return;
        }
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        Notification notification = new Notification();
        notification.setUser(userRepository.getReferenceById(userId));
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());

        if (taskId != null) {
            if (!taskRepository.existsById(taskId)) {
                throw new TaskNotFoundException(taskId);
            }
            notification.setTask(taskRepository.getReferenceById(taskId));
        }

        notificationRepository.save(notification);
        log.info("Создано уведомление для пользователя ID {}: {}", userId, message);
    }

    @Transactional
    public void createNotification(Long userId, String message) {
        createNotification(userId, null, message);
    }

    @Transactional
    public void send(User user, Task task, String message) {
        if (user == null) {
            log.warn("Попытка отправить уведомление null пользователю");
            return;
        }
        Long taskId = (task != null) ? task.getId() : null;
        createNotification(user.getId(), taskId, message);
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getLastNotifications(Long userId) {
        return notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public int sendUrgentReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursLater = now.plusHours(2);
        List<Task> urgentTasks = taskRepository.findAllByDeadlineBetween(now, twoHoursLater);

        int sentCount = 0;
        for (Task task : urgentTasks) {
            try {
                String message = "До дедлайна задачи '" + task.getTitle() + "' осталось меньше 2 часов";
                createNotification(task.getArtistId(), task.getId(), message);
                sentCount++;
            } catch (Exception e) {
                log.error("Не удалось отправить напоминание для задачи ID {}: {}", task.getId(), e.getMessage());
            }
        }
        log.info("Обработка срочных напоминаний завершена. Успешно отправлено: {}", sentCount);
        return sentCount;
    }

    @Transactional
    public void deleteAllByTaskId(Long taskId){
        notificationRepository.deleteAllByTaskId(taskId);
        log.info("Удалены все уведомления, связанные с задачей ID: {}", taskId);
    }

    @Transactional
    public long deleteOldNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        long deleted = notificationRepository.deleteAllByCreatedAtBefore(threshold);
        log.info("Удалено {} старых уведомлений", deleted);
        return deleted;
    }
}
