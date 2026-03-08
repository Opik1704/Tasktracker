package com.site.webapp.controllers;

import com.site.webapp.models.Tasks;
import com.site.webapp.models.User;
import com.site.webapp.repo.TasksRepository;
import com.site.webapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class TaskController extends LoggingController{
    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/all_tasks")
    public String allTasks(@RequestParam(defaultValue = "id") String sort,Model model){
        addUserToMDC();
        long startTime = System.currentTimeMillis();
        try {
            log.info("Пользователь,запросил список всех задач");
            log.debug("Параметры: sort = {}",sort);

            List<Tasks> tasks;
            List<User> users = userService.allUsers();

            if ("deadline".equals(sort)) {
                log.debug("Сортировка по дедлайну(убывание)");
                tasks = tasksRepository.findAllByOrderByDeadlineDesc();
            } else if ("priority".equals(sort)) {
                log.debug("Сортировка по приоритету(убывание)");
                tasks = tasksRepository.findAllByOrderByPriorityAsc();
            } else if ("id_desc".equals(sort)) {
                log.debug("Сортировка по id (убывание)");
                tasks = tasksRepository.findAllByOrderByIdDesc();
            } else {
                log.debug("Сортировка по умолчанию (по id возрастание)");
                tasks = tasksRepository.findAllByOrderByIdAsc();
                sort = "id_asc";
            }
            long duration = System.currentTimeMillis() - startTime;
            log.info("Загружено {} задач за {} мс", tasks.size(),duration);
            model.addAttribute("tasks", tasks);
            model.addAttribute("users", users);
            model.addAttribute("currentSort", sort);
            return "all_tasks";
        }
        finally {
            clearMDC();
        }
    }

    @PostMapping("/all_tasks")
    public String addTask(@RequestParam String title,
                          @RequestParam String priority,
                          @RequestParam Long artistId,
                          @RequestParam  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime deadline,
                          @RequestParam(required = false) String comment,
                          Model model){
        addUserToMDC();
        try {
            log.info("Создание новой задачи");
            log.debug("Данные: title='{}', priority={}, artistId={}, deadline={}",title, priority, artistId, deadline);

            Tasks task = new Tasks(title, priority, artistId, deadline, comment);
            tasksRepository.save(task);

            log.info("✅ Задача создана с ID: {}", task.getId());
            return "redirect:/all_tasks";
        }finally {
            clearMDC();
        }
    }
    @PostMapping("all_tasks/update")
    public String updateTask(@RequestParam Long id,
                             @RequestParam String title,
                             @RequestParam String priority,
                             @RequestParam Long artistId,
                             @RequestParam  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime deadline,
                             @RequestParam(required = false) String comment,
                             @RequestParam(defaultValue = "id_asc") String sort,
                             Model model) {
        addUserToMDC();
        try {
            log.info("Обновление задачи id {}",id);
            log.info("Пользователь {} обновляет задачу",getCurrentUserEmail());

            Tasks task = tasksRepository.findById(id).orElseThrow(() -> new RuntimeException("Задача не найдена"));
            log.debug("Старые данные title={},priority = {},artistId = {},deadline = {},comment = {}",
                    task.getTitle(),task.getPriority(),task.getArtistId(),task.getDeadline(),task.getComment());
            task.setTitle(title);
            task.setPriority(priority);
            task.setArtistId(artistId);
            task.setDeadline(deadline);
            task.setComment(comment);
            log.debug("Новые данные title={},priority = {},artistId = {},deadline = {},comment = {}",
                    task.getTitle(),task.getPriority(),task.getArtistId(),task.getDeadline(),task.getComment());
            tasksRepository.save(task);
            log.info("Задача id {} успешно обновлена", id);
            return "redirect:/all_tasks?sort=" + sort;
        }
        catch (Exception e){
            log.error("Ошибка при обновлении задачи {}: {}",id,e.getMessage(),e);
            throw e;
        }
        finally {
            clearMDC();
        }
    }
    @PostMapping("/all_tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, @RequestParam(defaultValue = "id_asc") String sort){
        addUserToMDC();
        try {
            log.info("Удаление задачи");
            tasksRepository.deleteById(id);
            log.info("Задача id {} удалена",id);
            return "redirect:/all_tasks?sort=" + sort;
        }
        finally {
            clearMDC();
        }
    }
    @GetMapping("/user_tasks")
    public String userTasks(@AuthenticationPrincipal User currentUser, Model model){
        addUserToMDC();
        long startTime = System.currentTimeMillis();
        try {
            log.info("Запрос задачей пользователя {}",getCurrentUserEmail());

            List<Tasks> tasks = tasksRepository.findByArtistId(currentUser.getId());
            List<User> users = userService.allUsers();
            long duration = System.currentTimeMillis() - startTime;
            log.info("Загружено {} задач за {} мс",tasks.size(),duration);
            model.addAttribute("tasks", tasks);
            model.addAttribute("users", users);
            return "user_tasks";
        }
        finally {
            clearMDC();
        }
    }
}
