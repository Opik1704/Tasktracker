package com.site.webapp.controllers;

import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TaskControllerTest {

    @Autowired
    private TaskController taskController;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;


    @Test
    void addTask() {
        String result = taskController.addTask("Тест задача 1","HIGH",1L, LocalDateTime.now(),"comment",null);
        assertEquals("redirect:/all_tasks",result);
        assertTrue(taskRepository.count() > 0);
    }
    @Test
    void updateTask() {
        LocalDateTime oldDeadline = LocalDateTime.now().plusDays(1);
        Task testTask = new Task("Старое название","HIGH",1L, oldDeadline,"comment");
        taskRepository.save(testTask);
        Long id = testTask.getId();

        LocalDateTime newDeadline = LocalDateTime.now().plusDays(3);
        taskController.updateTask(id,"Новое название","MEDIUM",2L,newDeadline,"new comment","id_asc",null);
        Task updated = taskRepository.findById(id).get();

        assertEquals("Новое название",updated.getTitle());
        assertEquals("MEDIUM",updated.getPriority());
        assertEquals(2L,updated.getArtistId());
        assertEquals(newDeadline,updated.getDeadline());
        assertEquals("new comment",updated.getComment());
    }

    @Test
    void deleteTask() {
        Task testTask = new Task("Тест задача 2","HIGH",1L, LocalDateTime.now(),"comment");
        taskRepository.save(testTask);
        Long id = testTask.getId();
        String sort = "id_asc";
        String result = taskController.deleteTask(id,sort);
        assertFalse(taskRepository.findById(id).isPresent());
        assertEquals("redirect:/all_tasks?sort=" + sort,result);
    }

    @Test
    void userTasks() {
        User user = new User();
        user.setEmail("simple@test.com");
        user.setPassword("123456");
        user.setFirstName("Семен");
        user.setLastName("Семеныч");
        userService.saveUser(user);

        taskRepository.save(new Task("Простая задача", "LOW", user.getId(), LocalDateTime.now(), null));

        Model model = new org.springframework.ui.ExtendedModelMap();
        String result = taskController.userTasks(user, model);

        assertEquals("user_tasks", result);

        List<Task> tasks = (List<Task>) model.getAttribute("tasks");
        assertEquals(1, tasks.size());
        assertEquals(user.getId(), tasks.get(0).getArtistId());
    }
}