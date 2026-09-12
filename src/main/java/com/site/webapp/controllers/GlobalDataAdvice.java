package com.site.webapp.controllers;

import com.site.webapp.models.Notification;
import com.site.webapp.repo.NotificationRepository;
import com.site.webapp.security.CustomUserDetails;
import com.site.webapp.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalDataAdvice extends LoggingController{

    private final NotificationRepository notificationRepository;

    public GlobalDataAdvice(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }


    @ModelAttribute
    public void addGlobalAttributes(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        if (currentUser != null) {
            log.trace("Наполнение глобальной модели для пользователя: {}", currentUser.getUsername());

            model.addAttribute("userAvatar", currentUser.getAvatarS3Key());
            model.addAttribute("currentUserName", currentUser.getFirstName());
            model.addAttribute("currentUserEmail", currentUser.getUsername());

            long unreadCount = notificationRepository.countByUserIdAndReadFalse(currentUser.getId());
            List<Notification> lastNotifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(currentUser.getId());

            model.addAttribute("notificationsCount", unreadCount);
            model.addAttribute("lastNotifications", lastNotifications);

            log.debug("Загружен хедер для {}: непрочитанных уведомлений - {}", currentUser.getUsername(), unreadCount);
        }
    }

}
