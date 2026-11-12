package com.site.webapp.service;

import com.site.webapp.models.Task;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.security.CustomUserDetails;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для TaskService (этап 2 — статистика на главной странице).
 * <p>
 * Проверяют:
 * - getStatistics: все показатели статистики (AC-1, AC-2, AC-3, AC-4, AC-5, AC-6)
 * - Фильтрация по ownerId (AC-1)
 * - Фильтрация мягко удалённых задач (AC-8)
 * - Обработка completedAt = null (AC-7, граничный случай 7)
 * - Расчёт среднего времени выполнения (AC-3)
 * - Расчёт процента выполнено в срок (AC-6)
 * - Переход IN_PROGRESS → COMPLETED заполняет completedAt (AC-10)
 * - Переход COMPLETED → IN_PROGRESS очищает completedAt (AC-9)
 * - Повторное редактирование COMPLETED не меняет completedAt (AC-11)
 * - createTask устанавливает статус NEW, completedAt = null
 * <p>
 * Зависимости: TaskService, TaskRepository (реализуются на этапе 2).
 */
@SpringBootTest
@Transactional
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

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

        var user = new com.site.webapp.models.User();
        user.setFirstName("Owner");
        user.setLastName("Ownerov");
        user.setEmail("owner@test.com");
        user.setPassword("password");
        user = userRepository.save(user);
        ownerId = user.getId();
        ownerDetails = new CustomUserDetails(user);
    }

    // === getStatistics ===

    @Test
    void getStatistics_noTasks_returnsZerosAndNullFastest() {
        var stats = taskService.getStatistics(ownerId);

        assertEquals(0, stats.getTotalCompleted());
        assertEquals(0.0, stats.getAverageCompletionHours(), 0.01);
        assertNull(stats.getFastestCompletedTask());
        assertEquals(0, stats.getCompletedThisWeek());
        assertEquals(0, stats.getCompletedThisMonth());
        assertEquals(0.0, stats.getOnTimePercentage(), 0.01);
    }

    @Test
    void getStatistics_onlyNotCompletedTasks_returnsZeros() {
        var now = LocalDateTime.now();
        var task1 = new Task("Task1", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.NEW, null);
        var task2 = new Task("Task2", Task.Priority.MEDIUM, 10L, ownerId,
                now.plusDays(2), Task.TaskStatus.IN_PROGRESS, null);
        taskRepository.save(task1);
        taskRepository.save(task2);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(0, stats.getTotalCompleted());
        assertEquals(0.0, stats.getAverageCompletionHours(), 0.01);
        assertNull(stats.getFastestCompletedTask());
        assertEquals(0.0, stats.getOnTimePercentage(), 0.01);
    }

    @Test
    void getStatistics_oneCompletedTask_returnsCorrectMetrics() {
        var now = LocalDateTime.now();
        var task = new Task("Task1", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now.minusDays(2));
        task.setCompletedAt(now.minusDays(1)); // 1 day = 24 hours
        task.setDeadline(now); // completed before deadline
        taskRepository.save(task);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(1, stats.getTotalCompleted());
        assertEquals(24.0, stats.getAverageCompletionHours(), 0.1);
        assertNotNull(stats.getFastestCompletedTask());
        assertEquals(task.getId(), stats.getFastestCompletedTask().getId());
        assertEquals(100.0, stats.getOnTimePercentage(), 0.01);
    }

    @Test
    void getStatistics_filtersByOwnerId() {
        var now = LocalDateTime.now();
        var taskOwner1 = new Task("TaskOwner1", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        taskOwner1.setCreatedAt(now.minusDays(2));
        taskOwner1.setCompletedAt(now.minusDays(1));
        taskRepository.save(taskOwner1);

        var otherOwnerId = 999L;
        var taskOther = new Task("TaskOther", Task.Priority.HIGH, 10L, otherOwnerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        taskOther.setCreatedAt(now.minusDays(2));
        taskOther.setCompletedAt(now.minusDays(1));
        taskRepository.save(taskOther);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(1, stats.getTotalCompleted());
        assertNotNull(stats.getFastestCompletedTask());
        assertEquals(taskOwner1.getId(), stats.getFastestCompletedTask().getId());
    }

    @Test
    void getStatistics_excludesDeletedTasks() {
        var now = LocalDateTime.now();
        var taskDeleted = new Task("Deleted", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        taskDeleted.setCreatedAt(now.minusDays(2));
        taskDeleted.setCompletedAt(now.minusDays(1));
        taskDeleted.setDeleted(true);
        taskRepository.save(taskDeleted);

        var taskActive = new Task("Active", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        taskActive.setCreatedAt(now.minusDays(2));
        taskActive.setCompletedAt(now.minusDays(1));
        taskRepository.save(taskActive);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(1, stats.getTotalCompleted());
    }

    @Test
    void getStatistics_completedAtNull_excludedFromTimeMetrics() {
        var now = LocalDateTime.now();
        var taskWithCompletedAt = new Task("WithCompletedAt", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        taskWithCompletedAt.setCreatedAt(now.minusDays(2));
        taskWithCompletedAt.setCompletedAt(now.minusDays(1)); // 24 hours
        taskWithCompletedAt.setDeadline(now);
        taskRepository.save(taskWithCompletedAt);

        var taskWithoutCompletedAt = new Task("WithoutCompletedAt", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        taskWithoutCompletedAt.setCreatedAt(now.minusDays(2));
        // completedAt remains null
        taskWithoutCompletedAt.setDeadline(now);
        taskRepository.save(taskWithoutCompletedAt);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(2, stats.getTotalCompleted()); // both counted as completed
        assertEquals(24.0, stats.getAverageCompletionHours(), 0.1); // only taskWithCompletedAt
        assertNotNull(stats.getFastestCompletedTask());
        assertEquals(taskWithCompletedAt.getId(), stats.getFastestCompletedTask().getId());
    }

    @Test
    void getStatistics_onTimePercentage_zeroWhenNoDeadlineTasks() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now.minusDays(2));
        task.setCompletedAt(now.minusDays(1));
        task.setDeadline(null); // no deadline
        taskRepository.save(task);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(0.0, stats.getOnTimePercentage(), 0.01);
    }

    @Test
    void getStatistics_onTimePercentage_calculatesCorrectly() {
        var now = LocalDateTime.now();
        // On time: completed before deadline
        var task1 = new Task("Task1", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        task1.setCreatedAt(now.minusDays(5));
        task1.setCompletedAt(now.minusDays(3));
        task1.setDeadline(now.minusDays(1));
        taskRepository.save(task1);

        // Late: completed after deadline
        var task2 = new Task("Task2", Task.Priority.MEDIUM, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        task2.setCreatedAt(now.minusDays(5));
        task2.setCompletedAt(now.minusDays(1));
        task2.setDeadline(now.minusDays(2));
        taskRepository.save(task2);

        // Exactly at deadline: on time
        var task3 = new Task("Task3", Task.Priority.TRIVIAL, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        task3.setCreatedAt(now.minusDays(5));
        task3.setCompletedAt(now.minusDays(2));
        task3.setDeadline(now.minusDays(2));
        taskRepository.save(task3);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(3, stats.getTotalCompleted());
        // 2 on time out of 3 with deadline = 66.67%
        assertEquals(66.67, stats.getOnTimePercentage(), 0.1);
    }

    @Test
    void getStatistics_completedThisWeekAndMonth_usesFixedDatesAndHalfOpenIntervals() {
        // Use the same time zone and reference as the service: Europe/Moscow, current time
        ZoneId moscow = ZoneId.of("Europe/Moscow");
        ZonedDateTime nowZoned = ZonedDateTime.now(moscow);
        LocalDateTime now = nowZoned.toLocalDateTime();

        // Week boundaries: Monday 00:00 to next Monday 00:00
        LocalDateTime mondayStart = now.toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime nextMondayStart = mondayStart.plusDays(7);

        // Month boundaries: 1st of month 00:00 to 1st of next month 00:00
        LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);

        // Task completed inside this week (inside [mondayStart, nextMondayStart))
        var taskThisWeek = new Task("ThisWeek", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        taskThisWeek.setCreatedAt(now.minusDays(1));
        taskThisWeek.setCompletedAt(mondayStart.plusDays(2)); // Wednesday of this week
        taskRepository.save(taskThisWeek);

        // Task completed exactly at week start (inclusive boundary)
        var taskAtWeekStart = new Task("AtWeekStart", Task.Priority.MEDIUM, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        taskAtWeekStart.setCreatedAt(now.minusDays(2));
        taskAtWeekStart.setCompletedAt(mondayStart); // exactly at start - included
        taskRepository.save(taskAtWeekStart);

        // Task completed exactly at next week start (exclusive boundary)
        var taskAtNextWeekStart = new Task("AtNextWeekStart", Task.Priority.TRIVIAL, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        taskAtNextWeekStart.setCreatedAt(now.minusDays(3));
        taskAtNextWeekStart.setCompletedAt(nextMondayStart); // exactly at end - excluded
        taskRepository.save(taskAtNextWeekStart);

        // Task completed before week start (excluded from week)
        var taskBeforeWeek = new Task("BeforeWeek", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        taskBeforeWeek.setCreatedAt(now.minusDays(4));
        taskBeforeWeek.setCompletedAt(mondayStart.minusSeconds(1)); // just before start
        taskRepository.save(taskBeforeWeek);

        // Task completed inside this month but outside this week
        var taskThisMonth = new Task("ThisMonth", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        taskThisMonth.setCreatedAt(now.minusDays(5));
        taskThisMonth.setCompletedAt(monthStart.plusDays(20)); // mid-to-late month, clearly outside week
        taskRepository.save(taskThisMonth);

        // Task completed exactly at next month start (exclusive boundary)
        var taskAtNextMonthStart = new Task("AtNextMonthStart", Task.Priority.MEDIUM, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        taskAtNextMonthStart.setCreatedAt(now.minusDays(6));
        taskAtNextMonthStart.setCompletedAt(nextMonthStart); // exactly at end - excluded
        taskRepository.save(taskAtNextMonthStart);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(6, stats.getTotalCompleted());
        // completedThisWeek: taskThisWeek + taskAtWeekStart = 2
        //   (taskAtNextWeekStart excluded - end boundary, taskBeforeWeek excluded - before start)
        assertEquals(2, stats.getCompletedThisWeek());
        // completedThisMonth: taskThisWeek + taskAtWeekStart + taskAtNextWeekStart + taskBeforeWeek + taskThisMonth = 5
        //   (taskAtNextMonthStart excluded - end boundary)
        assertEquals(5, stats.getCompletedThisMonth());
    }

    // === createTask: status NEW, completedAt null ===

    @Test
    void createTask_setsStatusNewAndCompletedAtNull() {
        var task = new Task("New Task", Task.Priority.HIGH, 10L, ownerId,
                LocalDateTime.now().plusDays(1), Task.TaskStatus.NEW, null);

        var created = taskService.createTask(task, ownerDetails);

        assertEquals(Task.TaskStatus.NEW, created.getStatus());
        assertNull(created.getCompletedAt());
    }

    // === updateTask: status transitions ===

    @Test
    void updateTask_inProgressToCompleted_setsCompletedAt() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.IN_PROGRESS, null);
        task.setCreatedAt(now);
        task = taskRepository.save(task);

        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(Task.TaskStatus.COMPLETED, saved.getStatus());
        assertNotNull(saved.getCompletedAt());
    }

    @Test
    void updateTask_completedToInProgress_clearsCompletedAt() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now);
        task.setCompletedAt(now.minusHours(1));
        task = taskRepository.save(task);

        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.IN_PROGRESS, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(Task.TaskStatus.IN_PROGRESS, saved.getStatus());
        assertNull(saved.getCompletedAt());
    }

    @Test
    void updateTask_completedToCancelled_clearsCompletedAt() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now);
        task.setCompletedAt(now.minusHours(1));
        task = taskRepository.save(task);

        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.CANCELLED, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(Task.TaskStatus.CANCELLED, saved.getStatus());
        assertNull(saved.getCompletedAt());
    }

    @Test
    void updateTask_completedToArchived_clearsCompletedAt() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now);
        task.setCompletedAt(now.minusHours(1));
        task = taskRepository.save(task);

        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.ARCHIVED, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(Task.TaskStatus.ARCHIVED, saved.getStatus());
        assertNull(saved.getCompletedAt());
    }

    @Test
    void updateTask_completedToReview_clearsCompletedAt() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now);
        task.setCompletedAt(now.minusHours(1));
        task = taskRepository.save(task);

        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.REVIEW, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(Task.TaskStatus.REVIEW, saved.getStatus());
        assertNull(saved.getCompletedAt());
    }

    @Test
    void updateTask_completedToTesting_clearsCompletedAt() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now);
        task.setCompletedAt(now.minusHours(1));
        task = taskRepository.save(task);

        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.TESTING, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(Task.TaskStatus.TESTING, saved.getStatus());
        assertNull(saved.getCompletedAt());
    }

    @Test
    void updateTask_completedToNew_clearsCompletedAt() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now);
        task.setCompletedAt(now.minusHours(1));
        task = taskRepository.save(task);

        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.NEW, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(Task.TaskStatus.NEW, saved.getStatus());
        assertNull(saved.getCompletedAt());
    }

    @Test
    void updateTask_completedToCompleted_doesNotChangeCompletedAt() {
        var now = LocalDateTime.now();
        var originalCompletedAt = now.minusHours(5);
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now);
        task.setCompletedAt(originalCompletedAt);
        task = taskRepository.save(task);

        var updated = new Task("Updated Title", Task.Priority.CRITICAL, 10L, ownerId,
                now.plusDays(2), Task.TaskStatus.COMPLETED, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());
        updated.setCompletedAt(originalCompletedAt);

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(Task.TaskStatus.COMPLETED, saved.getStatus());
        assertEquals(originalCompletedAt, saved.getCompletedAt());
        assertEquals("Updated Title", saved.getTitle());
    }

    @Test
    void updateTask_statusUnchanged_doesNotChangeCompletedAt() {
        var now = LocalDateTime.now();
        var originalCompletedAt = now.minusHours(3);
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now);
        task.setCompletedAt(originalCompletedAt);
        task = taskRepository.save(task);

        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());
        updated.setCompletedAt(originalCompletedAt);

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(originalCompletedAt, saved.getCompletedAt());
    }

    @Test
    void updateTask_newToCompleted_setsCompletedAt() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.NEW, null);
        task.setCreatedAt(now);
        task = taskRepository.save(task);

        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.plusDays(1), Task.TaskStatus.COMPLETED, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(Task.TaskStatus.COMPLETED, saved.getStatus());
        assertNotNull(saved.getCompletedAt());
    }

    // === Boundary cases ===

    @Test
    void getStatistics_completedExactlyAtDeadline_countsAsOnTime() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now.minusDays(2));
        var deadline = now.minusDays(1);
        task.setDeadline(deadline);
        task.setCompletedAt(deadline); // exactly at deadline
        taskRepository.save(task);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(100.0, stats.getOnTimePercentage(), 0.01);
    }

    @Test
    void getStatistics_completedOneSecondAfterDeadline_notOnTime() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now.minusDays(2));
        var deadline = now.minusDays(1);
        task.setDeadline(deadline);
        task.setCompletedAt(deadline.plusSeconds(1)); // 1 second after deadline
        taskRepository.save(task);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(0.0, stats.getOnTimePercentage(), 0.01);
    }

    @Test
    void getStatistics_oneTask_averageEqualsTaskDuration() {
        var now = LocalDateTime.now();
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        task.setCreatedAt(now.minusDays(2));
        task.setCompletedAt(now.minusDays(1)); // 24 hours
        taskRepository.save(task);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(1, stats.getTotalCompleted());
        assertEquals(24.0, stats.getAverageCompletionHours(), 0.1);
        assertNotNull(stats.getFastestCompletedTask());
        assertEquals(task.getId(), stats.getFastestCompletedTask().getId());
    }

    @Test
    void getStatistics_softDeletedTasks_notCountedInAnyMetric() {
        var now = LocalDateTime.now();
        var taskDeleted = new Task("Deleted", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        taskDeleted.setCreatedAt(now.minusDays(2));
        taskDeleted.setCompletedAt(now.minusDays(1));
        taskDeleted.setDeleted(true);
        taskRepository.save(taskDeleted);

        var stats = taskService.getStatistics(ownerId);

        assertEquals(0, stats.getTotalCompleted());
        assertEquals(0.0, stats.getAverageCompletionHours(), 0.01);
        assertNull(stats.getFastestCompletedTask());
        assertEquals(0.0, stats.getOnTimePercentage(), 0.01);
    }

    // === Deadline rules (AC-13–AC-17) ===

    @Test
    void createTask_withFutureDeadline_succeeds() {
        var futureDeadline = LocalDateTime.now().plusDays(7);
        var task = new Task("Future Deadline Task", Task.Priority.HIGH, 10L, ownerId,
                futureDeadline, Task.TaskStatus.NEW, null);

        var created = taskService.createTask(task, ownerDetails);

        assertNotNull(created.getId());
        assertEquals(futureDeadline, created.getDeadline());
        assertEquals(Task.TaskStatus.NEW, created.getStatus());
    }

    @Test
    void createTask_withNullDeadline_succeeds() {
        var task = new Task("No Deadline Task", Task.Priority.HIGH, 10L, ownerId,
                null, Task.TaskStatus.NEW, null);

        var created = taskService.createTask(task, ownerDetails);

        assertNotNull(created.getId());
        assertNull(created.getDeadline());
        assertEquals(Task.TaskStatus.NEW, created.getStatus());
    }

    @Test
    void createTask_withPastDeadline_rejected() {
        // AC-15: Новый дедлайн в прошлом должен быть отклонён при создании.
        // @Future на поле deadline вызывает ConstraintViolationException при вставке.
        var pastDeadline = LocalDateTime.now().minusDays(2);
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                pastDeadline, Task.TaskStatus.NEW, null);

        assertThrows(jakarta.validation.ConstraintViolationException.class,
                () -> taskService.createTask(task, ownerDetails));
    }

    @Test
    void updateTask_withChangedPastDeadline_rejected() {
        // AC-15: Изменение дедлайна на прошлое при редактировании должно быть отклонено.
        // Сначала создаём задачу с будущим дедлайном (чтобы она сохранилась).
        var now = LocalDateTime.now();
        var futureDeadline = now.plusDays(7);
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                futureDeadline, Task.TaskStatus.NEW, null);
        task = taskRepository.save(task);

        // Пытаемся изменить дедлайн на прошлое.
        var pastDeadline = now.minusDays(2);
        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                pastDeadline, Task.TaskStatus.NEW, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        assertThrows(jakarta.validation.ConstraintViolationException.class,
                () -> taskService.updateTask(updated, ownerDetails));
    }

    @Test
    void updateTask_toFutureDeadline_succeeds() {
        // AC-15: Изменение дедлайна на будущую дату должно успешно сохраняться.
        var now = LocalDateTime.now();
        var initialDeadline = now.plusDays(7);
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                initialDeadline, Task.TaskStatus.NEW, null);
        task = taskRepository.save(task);

        var newDeadline = now.plusDays(14);
        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                newDeadline, Task.TaskStatus.NEW, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(newDeadline, saved.getDeadline());
    }

    @Test
    void updateTask_withUnchangedOverdueDeadline_succeeds() {
        // AC-15: Если дедлайн не изменился (даже если он уже просрочен), задача сохраняется.
        var now = LocalDateTime.now();
        var overdueDeadline = now.minusDays(2);
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                overdueDeadline, Task.TaskStatus.NEW, null);
        task.setCreatedAt(now);
        task = taskRepository.save(task);

        var updated = new Task("Updated Task", Task.Priority.CRITICAL, 10L, ownerId,
                overdueDeadline, Task.TaskStatus.NEW, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(overdueDeadline, saved.getDeadline());
        assertEquals("Updated Task", saved.getTitle());
    }

    @Test
    void editOverdueTask_withoutChangingDeadline_succeeds() {
        // AC-16: Редактирование просроченной задачи без изменения дедлайна — успешно.
        var now = LocalDateTime.now();
        var overdueDeadline = now.minusDays(5);
        var task = new Task("Overdue Task", Task.Priority.HIGH, 10L, ownerId,
                overdueDeadline, Task.TaskStatus.IN_PROGRESS, null);
        task.setCreatedAt(now);
        task = taskRepository.save(task);

        var updated = new Task("Updated Overdue Task", Task.Priority.CRITICAL, 10L, ownerId,
                overdueDeadline, Task.TaskStatus.IN_PROGRESS, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(overdueDeadline, saved.getDeadline());
        assertEquals("Updated Overdue Task", saved.getTitle());
        assertEquals(Task.TaskStatus.IN_PROGRESS, saved.getStatus());
    }

    @Test
    void updateTask_clearDeadline_succeeds() {
        // AC-17: Очистка существующего дедлайна (deadline = null) при редактировании — успешно.
        var now = LocalDateTime.now();
        var futureDeadline = now.plusDays(7);
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                futureDeadline, Task.TaskStatus.NEW, null);
        task.setCreatedAt(now);
        task = taskRepository.save(task);

        var updated = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                null, Task.TaskStatus.NEW, null);
        updated.setId(task.getId());
        updated.setVersion(task.getVersion());
        updated.setCreatedAt(task.getCreatedAt());

        taskService.updateTask(updated, ownerDetails);

        var saved = taskRepository.findById(task.getId()).orElseThrow();
        assertNull(saved.getDeadline());
    }

    // === @CreationTimestamp verification ===

    @Autowired
    private EntityManager entityManager;

    @Test
    void createdAt_isSetByHibernateWhenNotManuallySet() {
        // Проверка реального поведения @CreationTimestamp при сохранении через репозиторий.
        // Hibernate должен установить createdAt автоматически при вставке.
        // Не устанавливаем createdAt вручную — должно установиться Hibernate.
        var task = new Task("CreatedAt Test", Task.Priority.HIGH, 10L, ownerId,
                LocalDateTime.now().plusDays(1), Task.TaskStatus.NEW, null);

        var saved = taskRepository.save(task);
        entityManager.flush();
        entityManager.clear();

        var reloaded = taskRepository.findById(saved.getId()).orElseThrow();
        assertNotNull(reloaded.getCreatedAt(),
                "createdAt должен быть установлен Hibernate при вставке");
    }

    @Test
    void createdAt_isNotOverwrittenByManualSetOnUpdate() {
        // Проверка: при обновлении через сервис createdAt не должен меняться,
        // так как поле помечено @Column(updatable = false).
        // Сначала сохраняем сущность с автоматически установленным createdAt.
        var task = new Task("Task", Task.Priority.HIGH, 10L, ownerId,
                LocalDateTime.now().plusDays(1), Task.TaskStatus.NEW, null);
        task = taskRepository.save(task);
        entityManager.flush();
        entityManager.clear();

        var reloaded = taskRepository.findById(task.getId()).orElseThrow();
        var originalCreatedAt = reloaded.getCreatedAt();
        assertNotNull(originalCreatedAt, "createdAt должен быть установлен при создании");

        // Обновляем другое поле, пытаясь установить новый createdAt
        var updated = new Task("Updated", Task.Priority.CRITICAL, 10L, ownerId,
                LocalDateTime.now().plusDays(2), Task.TaskStatus.NEW, null);
        updated.setId(reloaded.getId());
        updated.setVersion(reloaded.getVersion());
        updated.setCreatedAt(LocalDateTime.now().plusDays(10)); // Пытаемся установить новое createdAt

        taskService.updateTask(updated, ownerDetails);
        entityManager.flush();
        entityManager.clear();

        var savedAgain = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(originalCreatedAt, savedAgain.getCreatedAt(),
                "createdAt не должен изменяться при обновлении (updatable = false)");
    }
}
