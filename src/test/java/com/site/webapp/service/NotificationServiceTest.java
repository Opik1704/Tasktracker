package com.site.webapp.service;

import com.site.webapp.dto.NotificationDto;
import com.site.webapp.exception.notification.NotificationNotFoundException;
import com.site.webapp.exception.security.AccessDeniedException;
import com.site.webapp.models.Notification;
import com.site.webapp.models.User;
import com.site.webapp.repo.NotificationRepository;
import com.site.webapp.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User other;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User();
        owner.setEmail("owner@test.com");
        owner.setPassword("password");
        owner.setFirstName("Владелец");
        owner.setLastName("Уведомлений");
        owner = userRepository.save(owner);

        other = new User();
        other.setEmail("other@test.com");
        other.setPassword("password");
        other.setFirstName("Другой");
        other.setLastName("Пользователь");
        other = userRepository.save(other);
    }

    @Test
    void getNotifications_returnsOnlyOwnNotifications() {
        notificationService.createNotification(owner.getId(), "Уведомление 1");
        notificationService.createNotification(owner.getId(), "Уведомление 2");
        notificationService.createNotification(other.getId(), "Чужое уведомление");

        Page<NotificationDto> result = notificationService.getNotifications(
                owner.getId(), null, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertTrue(result.stream().allMatch(n -> n.getMessage().contains("Уведомление")));
    }

    @Test
    void getNotifications_filtersByReadFlag() {
        notificationService.createNotification(owner.getId(), "Непрочитанное");
        notificationService.createNotification(owner.getId(), "Прочитанное");

        Notification first = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(owner.getId()).get(1);
        first.setRead(true);
        notificationRepository.save(first);

        Page<NotificationDto> onlyUnread = notificationService.getNotifications(
                owner.getId(), false, PageRequest.of(0, 10));
        Page<NotificationDto> onlyRead = notificationService.getNotifications(
                owner.getId(), true, PageRequest.of(0, 10));

        assertEquals(1, onlyUnread.getTotalElements());
        assertEquals(1, onlyRead.getTotalElements());
    }

    @Test
    void getNotifications_paginatesResults() {
        for (int i = 0; i < 5; i++) {
            notificationService.createNotification(owner.getId(), "Уведомление " + i);
        }

        Page<NotificationDto> page = notificationService.getNotifications(
                owner.getId(), null, PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void markAsRead_marksOwnNotificationAsRead() {
        notificationService.createNotification(owner.getId(), "Тест");

        Notification saved = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(owner.getId()).get(0);
        assertFalse(saved.isRead());

        notificationService.markAsRead(owner.getId(), saved.getId());

        Notification updated = notificationRepository.findById(saved.getId()).orElseThrow();
        assertTrue(updated.isRead());
    }

    @Test
    void markAsRead_throwsForForeignNotification() {
        notificationService.createNotification(other.getId(), "Чужое");

        Notification foreign = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(other.getId()).get(0);

        assertThrows(NotificationNotFoundException.class,
                () -> notificationService.markAsRead(owner.getId(), foreign.getId()));
    }

    @Test
    void deleteNotification_deletesOwnNotification() {
        notificationService.createNotification(owner.getId(), "Удалить");

        Notification saved = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(owner.getId()).get(0);

        notificationService.deleteNotification(owner.getId(), saved.getId());

        assertFalse(notificationRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void deleteNotification_throwsForForeignNotification() {
        notificationService.createNotification(other.getId(), "Чужое");

        Notification foreign = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(other.getId()).get(0);

        assertThrows(NotificationNotFoundException.class,
                () -> notificationService.deleteNotification(owner.getId(), foreign.getId()));
    }

    @Test
    void markAsRead_throwsForNullUserId() {
        assertThrows(AccessDeniedException.class,
                () -> notificationService.markAsRead(null, 1L));
    }

    @Test
    void getNotifications_returnsEmptyPageForNoNotifications() {
        Page<NotificationDto> result = notificationService.getNotifications(
                owner.getId(), null, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}