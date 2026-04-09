package com.site.webapp.controllers;

import com.site.webapp.models.Notification;
import com.site.webapp.models.User;
import com.site.webapp.repo.NotificationRepository;
import com.site.webapp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalDataAdvice extends LoggingController{
    @Autowired
    NotificationService notificationService;
    @Autowired
    private NotificationRepository notificationRepository;


    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        addUserToMDC();
        try{
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                log.debug("Наполнение глобальной модели для пользователя: {}", currentUser.getEmail());

                User user = userRepository.findById(currentUser.getId()).orElse(null);
                long unreadCount = notificationRepository.countByUserIdAndReadFalse(currentUser.getId());

                if (user != null) {
                    model.addAttribute("userAvatar", user.getStoredAvatarFileName());
                    model.addAttribute("currentUserName", user.getFirstName());
                }

                model.addAttribute("notificationsCount", unreadCount);

                List<Notification> lastNotifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(currentUser.getId());
                model.addAttribute("lastNotifications", lastNotifications);

                model.addAttribute("currentUserName", currentUser.getEmail());
                log.info("Загружен хедер для {}: непрочитанных уведомлений - {}", currentUser.getEmail(), unreadCount);
            }
        }catch (Exception e) {
            log.error("Ошибка при наполнении глобальной модели данными: {}", e.getMessage());
        }finally {
            clearMDC();
        }
    }
}
