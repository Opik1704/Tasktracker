package com.site.webapp.service;

import com.site.webapp.models.Notification;
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


    public void send(User user,String message){
        if (user == null) throw new UsernameNotFoundException("Пользователь не найден");
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        log.info("Пользователю {} отправлено сообщение {}", user.getEmail(),message);
    }
    public void markAllAsRead(User user) {
        if (user == null) throw new UsernameNotFoundException("Пользователь не найден");
        List<Notification> unread = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        log.info("Пользователю {} отметил сообщения как прочитанные", user.getEmail());
    }

}
