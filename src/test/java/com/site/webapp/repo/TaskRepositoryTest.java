package com.site.webapp.repo;

import com.site.webapp.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        Task task1 = new Task("Task1","HIGH",10L,LocalDateTime.now(),null);
        Task task4 = new Task("Task4","CRITICAL",10L,LocalDateTime.now(),null);
        Task task3 = new Task("Task3","MEDIUM",2L,LocalDateTime.now(),null);
        Task task5 = new Task("Task5","TRIVIAL",40L,LocalDateTime.now(),null);
        taskRepository.save(task1);
        taskRepository.save(task3);
        taskRepository.save(task4);
        taskRepository.save(task5);

    }
    @Test
    void findByArtistId() {
        List<Task> artist1tasks = taskRepository.findByArtistId(10L);
        List<Task> artist2tasks = taskRepository.findByArtistId(2L);
        List<Task> artist3tasks = taskRepository.findByArtistId(1000L);
        assertEquals(2,artist1tasks.size());
        assertEquals(1,artist2tasks.size());
        assertEquals(0,artist3tasks.size());
        assertTrue(artist3tasks.isEmpty());
    }
    @Test
    void findAllByOrderByIdAsc() {
        List<Task> sortedTasks = taskRepository.findAllByOrderByIdAsc();
        assertTrue(sortedTasks.get(0).getId() < sortedTasks.get(1).getId());
        assertTrue(sortedTasks.get(1).getId() < sortedTasks.get(2).getId());
    }
    @Test
    void findAllByOrderByIdDesc() {
        List<Task> sortedDescTasks = taskRepository.findAllByOrderByIdDesc();
        assertTrue(sortedDescTasks.get(0).getId() > sortedDescTasks.get(1).getId());
        assertTrue(sortedDescTasks.get(1).getId() > sortedDescTasks.get(2).getId());
    }
    @Test
    void findAllByOrderByDeadlineDesc() {
        LocalDateTime now = LocalDateTime.now();
        Task task6 = new Task("Task6", "LOW", 1L, now.plusDays(1), "comment");
        Task task7 = new Task("Task7", "HIGH", 2L, now.plusDays(2), "comment");
        taskRepository.save(task6);
        taskRepository.save(task7);

        List<Task> sortedByDeadline = taskRepository.findAllByOrderByDeadlineDesc();
        assertTrue(sortedByDeadline.get(1).getDeadline().isBefore(sortedByDeadline.get(0).getDeadline()) );
    }

    @Test
    void findAllByOrderByPriorityAsc() {
        List<Task> sortedByPriority = taskRepository.findAllByOrderByPriorityAsc();

        System.out.println("Всего задач: " + sortedByPriority.size());

        for (Task task : sortedByPriority) {
            System.out.println(task.getPriority());
        }
        assertEquals("CRITICAL",sortedByPriority.get(0).getPriority());
        assertEquals("HIGH",sortedByPriority.get(1).getPriority());
        assertEquals("MEDIUM",sortedByPriority.get(2).getPriority());
        assertEquals("TRIVIAL",sortedByPriority.get(3).getPriority());

    }
}