package com.site.webapp.controllers;

import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для MainController (этап 2 — статистика на главной странице).
 * <p>
 * Проверяют:
 * - home() добавляет статистику в модель под именем "statistics" (AC-1)
 * - Статистика персональная (фильтрация по ownerId) (AC-1)
 * - Все шесть полей DTO заполнены корректно
 * - Пустая статистика: нули и null для fastestCompletedTask
 * <p>
 * Зависимости: MainController, TaskService (реализуются на этапе 2).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MainControllerTest {

    @Autowired
    private MainController mainController;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private Long ownerId;
    private CustomUserDetails ownerDetails;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        var user = new User();
        user.setFirstName("Owner");
        user.setLastName("Ownerov");
        user.setEmail("owner@test.com");
        user.setPassword("password");
        user = userRepository.save(user);
        ownerId = user.getId();
        ownerDetails = new CustomUserDetails(user);
    }

    @Test
    void home_addsStatisticsToModel() {
        var model = new ExtendedModelMap();
        var viewName = mainController.home(ownerDetails, model);

        assertEquals("home", viewName);
        assertTrue(model.containsAttribute("statistics"),
                "Модель должна содержать атрибут 'statistics'");
    }

    @Test
    void home_statisticsIsOfTaskStatisticsDtoType() {
        var model = new ExtendedModelMap();
        mainController.home(ownerDetails, model);

        var statistics = model.getAttribute("statistics");
        assertNotNull(statistics, "Атрибут 'statistics' не должен быть null");
        assertTrue(statistics instanceof com.site.webapp.dto.TaskStatisticsDto,
                "Атрибут 'statistics' должен быть экземпляром TaskStatisticsDto");
    }

    @Test
    void home_emptyStatistics_returnsZerosAndNullFastest() {
        var model = new ExtendedModelMap();
        mainController.home(ownerDetails, model);

        var statistics = model.getAttribute("statistics");
        assertNotNull(statistics);

        var dto = (com.site.webapp.dto.TaskStatisticsDto) statistics;
        assertEquals(0, dto.getTotalCompleted());
        assertEquals(0.0, dto.getAverageCompletionHours(), 0.01);
        assertNull(dto.getFastestCompletedTask());
        assertEquals(0, dto.getCompletedThisWeek());
        assertEquals(0, dto.getCompletedThisMonth());
        assertEquals(0.0, dto.getOnTimePercentage(), 0.01);
    }

    @Test
    void home_statisticsIsForCurrentUserOnly() {
        var now = LocalDateTime.now();
        // Task for owner - completed, completed before deadline (on time)
        var taskOwner = new Task("TaskOwner", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        taskOwner.setCreatedAt(now.minusDays(2));
        taskOwner.setCompletedAt(now.minusDays(1));
        taskOwner.setDeadline(now); // completedAt <= deadline -> on time
        taskRepository.save(taskOwner);

        // Task for owner - not completed (should not affect statistics)
        var taskOwnerNotCompleted = new Task("TaskOwnerNotCompleted", Task.Priority.MEDIUM, 10L, ownerId,
                now.minusDays(3), Task.TaskStatus.IN_PROGRESS, null);
        taskRepository.save(taskOwnerNotCompleted);

        // Task for another user - completed (should be excluded from statistics)
        var otherUser = new User();
        otherUser.setFirstName("Other");
        otherUser.setLastName("Otherov");
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("password");
        otherUser = userRepository.save(otherUser);
        var otherDetails = new CustomUserDetails(otherUser);

        var taskOther = new Task("TaskOther", Task.Priority.HIGH, 10L, otherUser.getId(),
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        taskOther.setCreatedAt(now.minusDays(2));
        taskOther.setCompletedAt(now.minusDays(1));
        taskRepository.save(taskOther);

        var model = new ExtendedModelMap();
        mainController.home(ownerDetails, model);

        var statistics = model.getAttribute("statistics");
        assertNotNull(statistics);

        var dto = (com.site.webapp.dto.TaskStatisticsDto) statistics;
        // Only owner's completed task should be counted
        assertEquals(1, dto.getTotalCompleted());
        assertNotNull(dto.getFastestCompletedTask());
        assertEquals(taskOwner.getId(), dto.getFastestCompletedTask().getId());
        // Other user's task must not influence statistics
        assertEquals(100.0, dto.getOnTimePercentage(), 0.01);
    }

    @Test
    void home_returnsHomeView() {
        var model = new ExtendedModelMap();
        var viewName = mainController.home(ownerDetails, model);

        assertEquals("home", viewName);
    }
}
