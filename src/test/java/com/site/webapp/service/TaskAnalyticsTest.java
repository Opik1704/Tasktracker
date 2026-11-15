package com.site.webapp.service;

import com.site.webapp.dto.TaskAnalyticsDto;
import com.site.webapp.models.Task;
import com.site.webapp.repo.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TaskAnalyticsTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    private final Long ownerId = 1L;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    private Task task(String title, Task.TaskStatus status, LocalDateTime deadline, LocalDateTime createdAt) {
        Task t = new Task(title, Task.Priority.MEDIUM, null, ownerId, deadline, status, null);
        t.setCreatedAt(createdAt);
        return taskRepository.save(t);
    }

    @Test
    void getAnalytics_emptyData_returnsZeros() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        TaskAnalyticsDto dto = taskService.getAnalytics(ownerId, start, end);

        assertEquals(0, dto.getTotalTasks());
        assertEquals(0, dto.getActiveTasks());
        assertEquals(0, dto.getCompletedTasks());
        assertEquals(0, dto.getOverdueTasks());
        assertEquals(0.0, dto.getCompletionPercentage());
        assertTrue(dto.getDailyCompletions().isEmpty());
    }

    @Test
    void getAnalytics_countsTotalsAndActive() {
        task("A", Task.TaskStatus.NEW, null, LocalDateTime.now());
        task("B", Task.TaskStatus.IN_PROGRESS, null, LocalDateTime.now());
        task("C", Task.TaskStatus.COMPLETED, null, LocalDateTime.now());

        TaskAnalyticsDto dto = taskService.getAnalytics(ownerId,
                LocalDateTime.now().minusDays(30), LocalDateTime.now().plusDays(1));

        assertEquals(3, dto.getTotalTasks());
        assertEquals(2, dto.getActiveTasks());
        assertEquals(1, dto.getCompletedTasks());
        assertEquals(33.33, dto.getCompletionPercentage(), 0.01);
    }

    @Test
    void getAnalytics_countsOverdueOnly() {
        task("Overdue", Task.TaskStatus.IN_PROGRESS, LocalDateTime.now().minusDays(2), LocalDateTime.now());
        task("NotOverdue", Task.TaskStatus.IN_PROGRESS, LocalDateTime.now().plusDays(2), LocalDateTime.now());
        task("CompletedOverdue", Task.TaskStatus.COMPLETED, LocalDateTime.now().minusDays(2), LocalDateTime.now());

        TaskAnalyticsDto dto = taskService.getAnalytics(ownerId,
                LocalDateTime.now().minusDays(30), LocalDateTime.now().plusDays(1));

        assertEquals(1, dto.getOverdueTasks());
    }

    @Test
    void getAnalytics_excludesSoftDeleted() {
        Task deleted = task("Deleted", Task.TaskStatus.NEW, null, LocalDateTime.now());
        deleted.setDeleted(true);
        taskRepository.save(deleted);

        TaskAnalyticsDto dto = taskService.getAnalytics(ownerId,
                LocalDateTime.now().minusDays(30), LocalDateTime.now().plusDays(1));

        assertEquals(0, dto.getTotalTasks());
    }

    @Test
    void getAnalytics_dailyCompletions_groupsByDay() {
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        Task t1 = task("Done1", Task.TaskStatus.COMPLETED, null, twoDaysAgo);
        t1.setCompletedAt(twoDaysAgo);
        taskRepository.save(t1);

        TaskAnalyticsDto dto = taskService.getAnalytics(ownerId,
                LocalDateTime.now().minusDays(30), LocalDateTime.now().plusDays(1));

        assertEquals(1, dto.getDailyCompletions().size());
        assertEquals(1, dto.getDailyCompletions().get(0).getCount());
    }

    @Test
    void getAnalytics_previousPeriodCounts() {
        // Предыдущий период для [now-10, now): [now-20, now-10)
        task("Old", Task.TaskStatus.NEW, null, LocalDateTime.now().minusDays(15));

        TaskAnalyticsDto dto = taskService.getAnalytics(ownerId,
                LocalDateTime.now().minusDays(10), LocalDateTime.now());

        assertEquals(1, dto.getPreviousPeriodCreated());
    }
}