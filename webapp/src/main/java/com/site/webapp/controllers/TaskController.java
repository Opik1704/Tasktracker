package com.site.webapp.controllers;

import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.service.NotificationService;
import com.site.webapp.service.TaskService;
import com.site.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class TaskController extends LoggingController{
    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

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

            List<Task> tasks = taskService.getAllTasks(sort,search);

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
    public String addTask(@Valid Task task, BindingResult bindingResult, Principal principal, Model model){
        addUserToMDC();
        try {
            if (bindingResult.hasErrors()) {
                log.warn("Ошибки валидации при создании задачи: {}", bindingResult.getAllErrors());
                model.addAttribute("tasks", taskService.getAllTasks("id", null));
                model.addAttribute("users", userService.allUsers());
                if (principal != null) {
                    model.addAttribute("currentUser", userService.findByEmail(principal.getName()));
                }
                return "all_tasks";
            }
            taskService.saveTask(task);
            log.info("Задача успешно создана");
            return "redirect:/all-tasks";
        }finally {
            clearMDC();
        }
    }

    @PostMapping("/all-tasks/update")
    public String updateTask(@Valid Task task,
                             BindingResult bindingResult,
                             @RequestParam(defaultValue = "id_asc") String sort,
                             Model model) {
        addUserToMDC();
        try {
            if (bindingResult.hasErrors()) {
                log.warn("Ошибки валидации при обновлении задачи ID {}: {}", task.getId(), bindingResult.getAllErrors());
                return "redirect:/all-tasks?sort=" + sort + "&error=validation";
            }
            taskService.updateTask(task.getId(), task.getTitle(), task.getPriority(),task.getArtistId(), task.getDeadline(), task.getComment());
            return "redirect:/all-tasks?sort=" + sort;
        }
        catch (Exception e){
            log.error("Ошибка при обновлении задачи {}",e.getMessage(),e);
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

            List<Task> tasks = taskService.getAllUserTasks(currentUser.getId(),sort,search);
            model.addAttribute("tasks", tasks);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("currentSort",sort);
            model.addAttribute("search",search);

            model.addAttribute("notificationsCount", notificationService.getUnreadCount(currentUser.getId()));
            model.addAttribute("lastNotifications", notificationService.getLastNotifications(currentUser.getId()));
            return "user_tasks";
        }
        finally {
            clearMDC();
        }
    }
}
