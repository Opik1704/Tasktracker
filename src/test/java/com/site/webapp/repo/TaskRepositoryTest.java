package com.site.webapp.repo;

import com.site.webapp.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        Task task1 = new Task("Task1", Task.Priority.HIGH, 10L, 1L, LocalDateTime.now().plusDays(1), Task.TaskStatus.NEW, null);
        Task task4 = new Task("Task4", Task.Priority.CRITICAL, 10L, 1L, LocalDateTime.now().plusDays(1), Task.TaskStatus.NEW, null);
        Task task3 = new Task("Task3", Task.Priority.MEDIUM, 2L, 1L, LocalDateTime.now().plusDays(1), Task.TaskStatus.NEW, null);
        Task task5 = new Task("Task5", Task.Priority.TRIVIAL, 40L, 1L, LocalDateTime.now().plusDays(1), Task.TaskStatus.NEW, null);
        taskRepository.save(task1);
        taskRepository.save(task3);
        taskRepository.save(task4);
        taskRepository.save(task5);

    }
    @Test
    void findByArtistId() {
        List<Task> artist1tasks = taskRepository.findByArtistId(10L, Sort.by("id"));
        List<Task> artist2tasks = taskRepository.findByArtistId(2L, Sort.by("id"));
        List<Task> artist3tasks = taskRepository.findByArtistId(1000L, Sort.by("id"));
        assertEquals(2, artist1tasks.size());
        assertEquals(1, artist2tasks.size());
        assertEquals(0, artist3tasks.size());
        assertTrue(artist3tasks.isEmpty());
    }
    @Test
    void findAllByOrderByIdAsc() {
        List<Task> sortedTasks = taskRepository.findAll(Sort.by("id"));
        assertTrue(sortedTasks.get(0).getId() < sortedTasks.get(1).getId());
        assertTrue(sortedTasks.get(1).getId() < sortedTasks.get(2).getId());
    }
    @Test
    void findAllByOrderByIdDesc() {
        List<Task> sortedDescTasks = taskRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        assertTrue(sortedDescTasks.get(0).getId() > sortedDescTasks.get(1).getId());
        assertTrue(sortedDescTasks.get(1).getId() > sortedDescTasks.get(2).getId());
    }
    @Test
    void findAllByOrderByDeadlineDesc() {
        LocalDateTime now = LocalDateTime.now();
        Task task6 = new Task("Task6", Task.Priority.TRIVIAL, 1L, 1L, now.plusDays(1), Task.TaskStatus.NEW, "comment");
        Task task7 = new Task("Task7", Task.Priority.HIGH, 2L, 1L, now.plusDays(2), Task.TaskStatus.NEW, "comment");
        taskRepository.save(task6);
        taskRepository.save(task7);

        List<Task> sortedByDeadline = taskRepository.findAll(Sort.by(Sort.Direction.DESC, "deadline"));
        assertTrue(sortedByDeadline.get(1).getDeadline().isBefore(sortedByDeadline.get(0).getDeadline()) );
    }

    @Test
    void findAllByOrderByPriorityAsc() {
        List<Task> sortedByPriority = taskRepository.findAll(Sort.by("priority"));

        System.out.println("Всего задач: " + sortedByPriority.size());

        for (Task task : sortedByPriority) {
            System.out.println(task.getPriority());
        }
        assertEquals(Task.Priority.CRITICAL, sortedByPriority.get(0).getPriority());
        assertEquals(Task.Priority.HIGH, sortedByPriority.get(1).getPriority());
        assertEquals(Task.Priority.MEDIUM, sortedByPriority.get(2).getPriority());
        assertEquals(Task.Priority.TRIVIAL, sortedByPriority.get(3).getPriority());

    }

    // === Stage 2: New repository methods for statistics ===

    @Test
    void countByOwnerIdAndStatusAndDeletedFalse_countsOnlyCompletedNotDeleted() {
        Long ownerId = 1L;
        Task completed1 = new Task("Completed1", Task.Priority.HIGH, 10L, ownerId,
                LocalDateTime.now().plusDays(1), Task.TaskStatus.COMPLETED, null);
        Task completed2 = new Task("Completed2", Task.Priority.MEDIUM, 10L, ownerId,
                LocalDateTime.now().plusDays(2), Task.TaskStatus.COMPLETED, null);
        Task notCompleted = new Task("NotCompleted", Task.Priority.TRIVIAL, 10L, ownerId,
                LocalDateTime.now().plusDays(3), Task.TaskStatus.IN_PROGRESS, null);
        Task deletedCompleted = new Task("DeletedCompleted", Task.Priority.HIGH, 10L, ownerId,
                LocalDateTime.now().plusDays(4), Task.TaskStatus.COMPLETED, null);
        deletedCompleted.setDeleted(true);
        Task otherOwnerCompleted = new Task("OtherOwnerCompleted", Task.Priority.HIGH, 10L, 999L,
                LocalDateTime.now().plusDays(5), Task.TaskStatus.COMPLETED, null);

        taskRepository.save(completed1);
        taskRepository.save(completed2);
        taskRepository.save(notCompleted);
        taskRepository.save(deletedCompleted);
        taskRepository.save(otherOwnerCompleted);

        long count = taskRepository.countByOwnerIdAndStatusAndDeletedFalse(ownerId, Task.TaskStatus.COMPLETED);
        assertEquals(2, count);
    }

    @Test
    void countByOwnerIdAndStatusAndDeletedFalse_returnsZeroWhenNoMatchingTasks() {
        Long ownerId = 1L;
        long count = taskRepository.countByOwnerIdAndStatusAndDeletedFalse(ownerId, Task.TaskStatus.COMPLETED);
        assertEquals(0, count);
    }

    @Test
    void findByOwnerIdAndStatusAndDeletedFalseAndCompletedAtIsNotNullOrderByCompletedAtAsc_returnsSortedByCompletedAt() {
        Long ownerId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Task t1 = new Task("T1", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(3), Task.TaskStatus.COMPLETED, null);
        t1.setCompletedAt(now.minusDays(2));
        Task t2 = new Task("T2", Task.Priority.MEDIUM, 10L, ownerId,
                now.minusDays(4), Task.TaskStatus.COMPLETED, null);
        t2.setCompletedAt(now.minusDays(1));
        Task t3 = new Task("T3", Task.Priority.TRIVIAL, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        t3.setCompletedAt(now);
        Task noCompletedAt = new Task("NoCompletedAt", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(6), Task.TaskStatus.COMPLETED, null);
        // completedAt remains null

        taskRepository.save(t1);
        taskRepository.save(t2);
        taskRepository.save(t3);
        taskRepository.save(noCompletedAt);

        List<Task> result = taskRepository.findByOwnerIdAndStatusAndDeletedFalseAndCompletedAtIsNotNullOrderByCompletedAtAsc(
                ownerId, Task.TaskStatus.COMPLETED);

        assertEquals(3, result.size());
        // ASC order by completedAt: t1 (now-2 days), t2 (now-1 day), t3 (now)
        assertEquals(t1.getId(), result.get(0).getId());
        assertEquals(t2.getId(), result.get(1).getId());
        assertEquals(t3.getId(), result.get(2).getId());
    }

    @Test
    void findByOwnerIdAndStatusAndDeletedFalseAndCompletedAtIsNotNullOrderByCompletedAtAsc_excludesNullCompletedAt() {
        Long ownerId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Task withCompletedAt = new Task("WithCompletedAt", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(1), Task.TaskStatus.COMPLETED, null);
        withCompletedAt.setCompletedAt(now);
        Task withoutCompletedAt = new Task("WithoutCompletedAt", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        // completedAt is null

        taskRepository.save(withCompletedAt);
        taskRepository.save(withoutCompletedAt);

        List<Task> result = taskRepository.findByOwnerIdAndStatusAndDeletedFalseAndCompletedAtIsNotNullOrderByCompletedAtAsc(
                ownerId, Task.TaskStatus.COMPLETED);

        assertEquals(1, result.size());
        assertEquals(withCompletedAt.getId(), result.get(0).getId());
    }

    @Test
    void findFastestCompletedTaskByOwnerIdAndStatus_returnsTaskWithMinDuration() {
        Long ownerId = 1L;
        // Deterministic dates: createdAt and completedAt are fixed, not relative to now
        LocalDateTime base = LocalDateTime.of(2026, 11, 1, 0, 0, 0);
        Task slow = new Task("Slow", Task.Priority.HIGH, 10L, ownerId,
                base.plusDays(10), Task.TaskStatus.COMPLETED, null);
        slow.setCreatedAt(base); // 2026-11-01
        slow.setCompletedAt(base.plusDays(10)); // 2026-11-11, duration: 9 days
        Task fast = new Task("Fast", Task.Priority.MEDIUM, 10L, ownerId,
                base.plusDays(2), Task.TaskStatus.COMPLETED, null);
        fast.setCreatedAt(base.plusDays(9)); // 2026-11-10
        fast.setCompletedAt(base.plusDays(10)); // 2026-11-11, duration: 1 day
        Task medium = new Task("Medium", Task.Priority.TRIVIAL, 10L, ownerId,
                base.plusDays(6), Task.TaskStatus.COMPLETED, null);
        medium.setCreatedAt(base.plusDays(6)); // 2026-11-07
        medium.setCompletedAt(base.plusDays(9)); // 2026-11-10, duration: 3 days

        taskRepository.save(slow);
        taskRepository.save(fast);
        taskRepository.save(medium);

        Task fastest = taskRepository.findFastestCompletedTaskByOwnerIdAndStatus(ownerId, Task.TaskStatus.COMPLETED);

        assertNotNull(fastest);
        assertEquals(fast.getId(), fastest.getId());
    }

    @Test
    void findFastestCompletedTaskByOwnerIdAndStatus_returnsOneOfTiedTasks() {
        Long ownerId = 1L;
        LocalDateTime now = LocalDateTime.now();
        // Both tasks have the same duration: 1 day
        Task taskA = new Task("TaskA", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(3), Task.TaskStatus.COMPLETED, null);
        taskA.setCreatedAt(now.minusDays(3));
        taskA.setCompletedAt(now.minusDays(2)); // duration: 1 day
        Task taskB = new Task("TaskB", Task.Priority.MEDIUM, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        taskB.setCreatedAt(now.minusDays(5));
        taskB.setCompletedAt(now.minusDays(4)); // duration: 1 day

        taskRepository.save(taskA);
        taskRepository.save(taskB);

        Task fastest = taskRepository.findFastestCompletedTaskByOwnerIdAndStatus(ownerId, Task.TaskStatus.COMPLETED);

        assertNotNull(fastest);
        // With equal duration, either taskA or taskB may be returned
        assertTrue(fastest.getId().equals(taskA.getId()) || fastest.getId().equals(taskB.getId()));
    }

    @Test
    void findFastestCompletedTaskByOwnerIdAndStatus_returnsNullWhenNoCompletedTasks() {
        Long ownerId = 1L;
        Task fastest = taskRepository.findFastestCompletedTaskByOwnerIdAndStatus(ownerId, Task.TaskStatus.COMPLETED);
        assertNull(fastest);
    }

    @Test
    void findCompletedByOwnerIdAndStatusAndPeriod_usesHalfOpenInterval() {
        Long ownerId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(7);

        Task inside = new Task("Inside", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(1), Task.TaskStatus.COMPLETED, null);
        inside.setCompletedAt(start.plusDays(3)); // inside [start, end)
        Task atStart = new Task("AtStart", Task.Priority.MEDIUM, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        atStart.setCompletedAt(start); // exactly at start - included
        Task atEnd = new Task("AtEnd", Task.Priority.TRIVIAL, 10L, ownerId,
                now.minusDays(3), Task.TaskStatus.COMPLETED, null);
        atEnd.setCompletedAt(end); // exactly at end - excluded (half-open)
        Task outside = new Task("Outside", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(4), Task.TaskStatus.COMPLETED, null);
        outside.setCompletedAt(start.minusDays(1)); // before start

        taskRepository.save(inside);
        taskRepository.save(atStart);
        taskRepository.save(atEnd);
        taskRepository.save(outside);

        List<Task> result = taskRepository.findCompletedByOwnerIdAndStatusAndPeriod(
                ownerId, Task.TaskStatus.COMPLETED, start, end);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getId().equals(inside.getId())));
        assertTrue(result.stream().anyMatch(t -> t.getId().equals(atStart.getId())));
        assertFalse(result.stream().anyMatch(t -> t.getId().equals(atEnd.getId())));
        assertFalse(result.stream().anyMatch(t -> t.getId().equals(outside.getId())));
    }

    @Test
    void countCompletedByOwnerIdAndStatusAndPeriod_matchesListSizeAndHalfOpenInterval() {
        Long ownerId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(7);

        Task inside = new Task("Inside", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(1), Task.TaskStatus.COMPLETED, null);
        inside.setCompletedAt(start.plusDays(3));
        Task atStart = new Task("AtStart", Task.Priority.MEDIUM, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        atStart.setCompletedAt(start);
        Task atEnd = new Task("AtEnd", Task.Priority.TRIVIAL, 10L, ownerId,
                now.minusDays(3), Task.TaskStatus.COMPLETED, null);
        atEnd.setCompletedAt(end);
        Task outside = new Task("Outside", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(4), Task.TaskStatus.COMPLETED, null);
        outside.setCompletedAt(start.minusDays(1));

        taskRepository.save(inside);
        taskRepository.save(atStart);
        taskRepository.save(atEnd);
        taskRepository.save(outside);

        long count = taskRepository.countCompletedByOwnerIdAndStatusAndPeriod(
                ownerId, Task.TaskStatus.COMPLETED, start, end);

        assertEquals(2, count);
        assertEquals(
                taskRepository.findCompletedByOwnerIdAndStatusAndPeriod(
                        ownerId, Task.TaskStatus.COMPLETED, start, end).size(),
                count);
    }

    @Test
    void countByOwnerIdAndStatusAndDeletedFalseAndCompletedAtIsNull_countsCompletedWithoutCompletedAt() {
        Long ownerId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Task withCompletedAt = new Task("WithCompletedAt", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(1), Task.TaskStatus.COMPLETED, null);
        withCompletedAt.setCompletedAt(now);
        Task withoutCompletedAt = new Task("WithoutCompletedAt", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(2), Task.TaskStatus.COMPLETED, null);
        // completedAt is null
        Task notCompleted = new Task("NotCompleted", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(3), Task.TaskStatus.IN_PROGRESS, null);

        taskRepository.save(withCompletedAt);
        taskRepository.save(withoutCompletedAt);
        taskRepository.save(notCompleted);

        long count = taskRepository.countByOwnerIdAndStatusAndDeletedFalseAndCompletedAtIsNull(
                ownerId, Task.TaskStatus.COMPLETED);

        assertEquals(1, count);
    }

    @Test
    void countOnTimeByOwnerIdAndStatus_countsCompletedBeforeOrAtDeadline() {
        Long ownerId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Task onTime = new Task("OnTime", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        onTime.setDeadline(now.minusDays(1));
        onTime.setCompletedAt(now.minusDays(2)); // completed before deadline
        Task late = new Task("Late", Task.Priority.MEDIUM, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        late.setDeadline(now.minusDays(3));
        late.setCompletedAt(now.minusDays(2)); // completed after deadline
        Task exactlyAtDeadline = new Task("ExactlyAtDeadline", Task.Priority.TRIVIAL, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        exactlyAtDeadline.setDeadline(now.minusDays(2));
        exactlyAtDeadline.setCompletedAt(now.minusDays(2)); // completed exactly at deadline - on time
        Task noDeadline = new Task("NoDeadline", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        noDeadline.setDeadline(null);
        noDeadline.setCompletedAt(now.minusDays(2)); // no deadline - excluded from on-time count
        Task noCompletedAt = new Task("NoCompletedAt", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        noCompletedAt.setDeadline(now.minusDays(1));
        // completedAt is null - excluded

        taskRepository.save(onTime);
        taskRepository.save(late);
        taskRepository.save(exactlyAtDeadline);
        taskRepository.save(noDeadline);
        taskRepository.save(noCompletedAt);

        long count = taskRepository.countOnTimeByOwnerIdAndStatus(ownerId, Task.TaskStatus.COMPLETED);

        assertEquals(2, count); // onTime and exactlyAtDeadline
    }

    @Test
    void countCompletedWithDeadlineByOwnerIdAndStatus_countsCompletedWithDeadlineAndCompletedAt() {
        Long ownerId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Task withBoth = new Task("WithBoth", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        withBoth.setDeadline(now.minusDays(1));
        withBoth.setCompletedAt(now.minusDays(2));
        Task noDeadline = new Task("NoDeadline", Task.Priority.MEDIUM, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        noDeadline.setDeadline(null);
        noDeadline.setCompletedAt(now.minusDays(2));
        Task noCompletedAt = new Task("NoCompletedAt", Task.Priority.TRIVIAL, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        noCompletedAt.setDeadline(now.minusDays(1));
        // completedAt is null
        Task notCompleted = new Task("NotCompleted", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.IN_PROGRESS, null);
        notCompleted.setDeadline(now.minusDays(1));

        taskRepository.save(withBoth);
        taskRepository.save(noDeadline);
        taskRepository.save(noCompletedAt);
        taskRepository.save(notCompleted);

        long count = taskRepository.countCompletedWithDeadlineByOwnerIdAndStatus(ownerId, Task.TaskStatus.COMPLETED);

        assertEquals(1, count); // only withBoth
    }

    @Test
    void averageCompletionHoursByOwnerIdAndStatus_returnsAverageInHours() {
        Long ownerId = 1L;
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task1", Task.Priority.HIGH, 10L, ownerId,
                now.minusDays(10), Task.TaskStatus.COMPLETED, null);
        task1.setCreatedAt(now.minusDays(10));
        task1.setCompletedAt(now.minusDays(8)); // 2 days = 48 hours
        Task task2 = new Task("Task2", Task.Priority.MEDIUM, 10L, ownerId,
                now.minusDays(5), Task.TaskStatus.COMPLETED, null);
        task2.setCreatedAt(now.minusDays(5));
        task2.setCompletedAt(now.minusDays(4)); // 1 day = 24 hours
        Task noCompletedAt = new Task("NoCompletedAt", Task.Priority.TRIVIAL, 10L, ownerId,
                now.minusDays(3), Task.TaskStatus.COMPLETED, null);
        // completedAt is null - excluded

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(noCompletedAt);

        Double avg = taskRepository.averageCompletionHoursByOwnerIdAndStatus(ownerId, Task.TaskStatus.COMPLETED);

        assertNotNull(avg);
        assertEquals(36.0, avg, 0.1); // (48 + 24) / 2 = 36 hours
    }

    @Test
    void averageCompletionHoursByOwnerIdAndStatus_returnsZeroWhenNoCompletedTasks() {
        Long ownerId = 1L;
        Double avg = taskRepository.averageCompletionHoursByOwnerIdAndStatus(ownerId, Task.TaskStatus.COMPLETED);
        assertEquals(0.0, avg, 0.01);
    }
}