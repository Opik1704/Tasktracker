package com.site.webapp.controllers;

import com.site.webapp.models.Tasks;
import com.site.webapp.models.User;
import com.site.webapp.repo.TasksRepository;
import com.site.webapp.service.TaskService;
import com.site.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class TaskController extends LoggingController{
    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @GetMapping("/all-tasks")
    public String allTasks(@RequestParam(defaultValue = "id") String sort,
                           @RequestParam(required = false) String search,
                           Principal principal,
                           Model model){
        addUserToMDC();
        long startTime = System.currentTimeMillis();
        try {
            log.info("Пользователь,запросил список всех задач");
            log.debug("Параметры: sort = {}",sort);

            List<Tasks> tasks = taskService.getAllTasks(sort,search);

            model.addAttribute("tasks", tasks);
            model.addAttribute("users",userService.allUsers());
            model.addAttribute("currentSort", sort);
            model.addAttribute("search",search);
            if (principal != null) {
                User currentUser = userService.findByEmail(principal.getName());
                model.addAttribute("currentUser", currentUser);
            }
            return "all_tasks";
        }
        finally {
            clearMDC();
        }
    }

    @PostMapping("/all-tasks")
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
            taskService.saveTask(new Tasks(title, priority, artistId, deadline, comment));
            return "redirect:/all-tasks";
        }finally {
            clearMDC();
        }
    }

    @PostMapping("/all-tasks/update")
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
            taskService.updateTask(id,title,priority,artistId,deadline,comment);
            return "redirect:/all-tasks?sort=" + sort;
        }
        catch (Exception e){
            log.error("Ошибка при обновлении задачи {}: {}",id,e.getMessage(),e);
            return "redirect:/all-tasks?sort=" + sort + "&error=true";
        }
        finally {
            clearMDC();
        }
    }

    @PostMapping("/all-tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, @RequestParam(defaultValue = "id_asc") String sort){
        addUserToMDC();
        try {
            log.info("Удаление задачи");
            taskService.deleteTask(id);
            log.info("Задача id {} удалена",id);
            return "redirect:/all-tasks?sort=" + sort;
        }
        finally {
            clearMDC();
        }
    }


    @GetMapping("/favorites")
    public String favorites(@RequestParam(required = false) String sort,
                            Principal principal,
                            Model model) {
        if (principal == null) return "redirect:/authorization";
        addUserToMDC();
        try {
            String email = principal.getName();
            log.info("Пользователь {} просматривает избранное (сортировка: {})", email, sort);

            model.addAttribute("tasks", taskService.getSortedFavorites(email, sort));
            model.addAttribute("users", userService.allUsers());
            model.addAttribute("pageTitle", "Избранные задачи");
            return "favorites";
        } finally {
            clearMDC();
        }
    }
    @PostMapping("/favorites/toggle/{taskId}")
    public String toggleFavorite(@PathVariable Long taskId,
                                 Principal principal,
                                 HttpServletRequest request) {
        if (principal == null) {
            log.warn("Попытка изменить избранное без авторизации");
            return "redirect:/authorization";
        }
        addUserToMDC();
        try {
            String email = principal.getName();
            log.info("Пользователь {} переключает избранное для задачи {}",email, taskId);
            taskService.toggleFavorite(principal.getName(),taskId);
            String referer = request.getHeader("Referer");
            if (referer != null) {
                return "redirect:" + referer;
            }
            return "redirect:/all-tasks";
        }finally {
            clearMDC();
        }
    }

    @GetMapping("/user-tasks")
    public String userTasks(@AuthenticationPrincipal User currentUser,
                            @RequestParam(defaultValue = "id_asc") String sort,
                            @RequestParam(required = false) String search,
                            Model model){
        if (currentUser == null) return "redirect:/authorization";
        addUserToMDC();
        try {
            log.info("Запрос задачей пользователя {}",getCurrentUserEmail());

            List<Tasks> tasks = taskService.getAllUserTasks(currentUser.getId(),sort,search);
            model.addAttribute("tasks", tasks);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("currentSort",sort);
            model.addAttribute("search",search);
            return "user_tasks";
        }
        finally {
            clearMDC();
        }
    }
}
