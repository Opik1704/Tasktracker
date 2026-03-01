package com.site.webapp.repo;

import com.site.webapp.models.Tasks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class TasksRepositoryTest {

    @Autowired
    private TasksRepository tasksRepository;
    @BeforeEach
    void setUp() {
        Tasks task1 = new Tasks("Task1","HIGH",1L,LocalDateTime.now(),null);
        Tasks task4 = new Tasks("Task4","CRITICAL",1L,LocalDateTime.now(),null);
        Tasks task3 = new Tasks("Task3","MEDIUM",2L,LocalDateTime.now(),null);
        Tasks task5 = new Tasks("Task5","TRIVIAL",4L,LocalDateTime.now(),null);
        tasksRepository.save(task1);
        tasksRepository.save(task3);
        tasksRepository.save(task4);

    }
    @Test
    void findByArtistId() {
        List<Tasks> artist1tasks = tasksRepository.findByArtistId(1L);
        List<Tasks> artist2tasks = tasksRepository.findByArtistId(2L);
        List<Tasks> artist3tasks = tasksRepository.findByArtistId(1000L);
        assertEquals(2,artist1tasks.size());
        assertEquals(1,artist2tasks.size());
        assertEquals(0,artist2tasks.size());
        assertTrue(artist3tasks.isEmpty());
    }
    @Test
    void findAllByOrderByIdAsc() {
        List<Tasks> sortedTasks = tasksRepository.findAllByOrderByIdAsc();
        assertTrue(sortedTasks.get(0).getId() < sortedTasks.get(1).getId());
        assertTrue(sortedTasks.get(1).getId() < sortedTasks.get(2).getId());
    }
    @Test
    void findAllByOrderByIdDesc() {
        List<Tasks> sortedDescTasks = tasksRepository.findAllByOrderByIdDesc();
        assertTrue(sortedDescTasks.get(0).getId() > sortedDescTasks.get(1).getId());
        assertTrue(sortedDescTasks.get(1).getId() > sortedDescTasks.get(2).getId());
    }
    @Test
    void findAllByOrderByDeadlineDesc() {
        LocalDateTime now = LocalDateTime.now();
        Tasks task6 = new Tasks("Task6", "LOW", 1L, now.plusDays(1), "comment");
        Tasks task7 = new Tasks("Task7", "HIGH", 2L, now.plusDays(2), "comment");
        tasksRepository.save(task6);
        tasksRepository.save(task7);

        List<Tasks> sortedByDeadline = tasksRepository.findAllByOrderByDeadlineDesc();
        assertTrue(sortedByDeadline.get(0).getDeadline().isBefore(sortedByDeadline.get(1).getDeadline()) );
    }

    @Test
    void findAllByOrderByPriorityAsc() {
        List<Tasks> sortedByPriority = tasksRepository.findAllByOrderByPriorityAsc();
        assertEquals("CRITICAL",sortedByPriority.get(0).getPriority());
        assertEquals("HIGH",sortedByPriority.get(1).getPriority());
        assertEquals("MEDIUM",sortedByPriority.get(2).getPriority());
        assertEquals("TRIVIAL",sortedByPriority.get(3).getPriority());

    }
}