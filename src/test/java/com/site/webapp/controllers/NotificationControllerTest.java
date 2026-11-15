package com.site.webapp.controllers;

import com.site.webapp.models.Notification;
import com.site.webapp.models.User;
import com.site.webapp.repo.NotificationRepository;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.security.CustomUserDetails;
import com.site.webapp.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class NotificationControllerTest {

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    private User owner;
    private CustomUserDetails ownerDetails;
    private User otherUser;
    private CustomUserDetails otherUserDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User();
        owner.setFirstName("Owner");
        owner.setLastName("Ownerov");
        owner.setEmail("owner@test.com");
        owner.setPassword("password");
        owner = userRepository.save(owner);
        ownerDetails = new CustomUserDetails(owner);

        otherUser = new User();
        otherUser.setFirstName("Other");
        otherUser.setLastName("Otherov");
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);
        otherUserDetails = new CustomUserDetails(otherUser);
    }

    @Test
    void list_returnsNotificationsPage() throws Exception {
        notificationService.createNotification(owner.getId(), "Тестовое уведомление");

        mockMvc.perform(get("/notifications").with(user(ownerDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attributeExists("notificationsCount"));
    }

    @Test
    void list_redirectsToLoginWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void unreadCount_returnsCount() throws Exception {
        notificationService.createNotification(owner.getId(), "Непрочитанное");

        mockMvc.perform(get("/notifications/unread-count").with(user(ownerDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void unreadCount_redirectsToLoginWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/notifications/unread-count"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void markAsRead_redirectsBack() throws Exception {
        notificationService.createNotification(owner.getId(), "Тест");

        Notification saved = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(owner.getId()).get(0);

        mockMvc.perform(post("/notifications/{id}/read", saved.getId())
                        .with(csrf())
                        .with(user(ownerDetails))
                        .header("referer", "/notifications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }

    @Test
    void delete_redirectsBack() throws Exception {
        notificationService.createNotification(owner.getId(), "Удалить");

        Notification saved = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(owner.getId()).get(0);

        mockMvc.perform(post("/notifications/{id}/delete", saved.getId())
                        .with(csrf())
                        .with(user(ownerDetails))
                        .header("referer", "/notifications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }

    @Test
    void markAsRead_externalReferer_isRejectedAsOpenRedirect() throws Exception {
        notificationService.createNotification(owner.getId(), "Тест");

        Notification saved = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(owner.getId()).get(0);

        mockMvc.perform(post("/notifications/{id}/read", saved.getId())
                        .with(csrf())
                        .with(user(ownerDetails))
                        .header("referer", "https://evil.com/phish"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }

    @Test
    void markAsRead_protocolRelativeReferer_isRejected() throws Exception {
        notificationService.createNotification(owner.getId(), "Тест");

        Notification saved = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(owner.getId()).get(0);

        mockMvc.perform(post("/notifications/{id}/read", saved.getId())
                        .with(csrf())
                        .with(user(ownerDetails))
                        .header("referer", "//evil.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }

    @Test
    void markAsRead_foreignNotification_isRejectedByOwnershipCheck() throws Exception {
        notificationService.createNotification(otherUser.getId(), "Чужое");

        Notification foreign = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(otherUser.getId()).get(0);

        // Уведомление не принадлежит текущему пользователю — операция отклоняется,
        // а глобальный обработчик перенаправляет обратно с сообщением об ошибке.
        mockMvc.perform(post("/notifications/{id}/read", foreign.getId())
                        .with(csrf())
                        .with(user(ownerDetails)))
                .andExpect(status().is3xxRedirection());

        // Уведомление осталось непрочитанным.
        Notification after = notificationRepository.findById(foreign.getId()).orElseThrow();
        assertFalse(after.isRead());
    }
}