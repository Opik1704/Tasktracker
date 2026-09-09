package com.site.webapp.controllers;

import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.service.TaskAttachmentService;
import com.site.webapp.service.TaskService;
import com.site.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
public class TaskController extends LoggingController{
    private final TaskService taskService;
    private final UserService userService;
    private final TaskAttachmentService taskAttachmentService;

    public TaskController(TaskService taskService, UserService userService,TaskAttachmentService taskAttachmentService) {
        this.taskService = taskService;
        this.userService = userService;
        this.taskAttachmentService = taskAttachmentService;
    }

    @GetMapping("/all-tasks")
    public String allTasks(@RequestParam(defaultValue = "id") String sort,
                           @RequestParam(required = false) String search,
                           Model model){
        addUserToMDC();
        try {
            log.info("Пользователь,запросил список всех задач");
            log.debug("Параметры: sort = {}",sort);

            List<Task> tasks = taskService.getAllTasks(sort,search);
            User currentUser = getCurrentUser();

            model.addAttribute("tasks", tasks);
            model.addAttribute("users",userService.allUsers());
            model.addAttribute("currentSort", sort);
            model.addAttribute("search",search);
            model.addAttribute("currentUser", currentUser);

            return "all_tasks";
        }
        finally {
            clearMDC();
        }
    }

    @PostMapping("/all-tasks")
    public String addTask(@Valid Task task,
                          BindingResult bindingResult,
                          @RequestParam(value = "file", required = false) MultipartFile file,
                          Model model) throws IOException {
        addUserToMDC();
        try {
            User currentUser = getCurrentUser();
            if (bindingResult.hasErrors()) {
                log.warn("Ошибки валидации при создании задачи: {}", bindingResult.getAllErrors());
                model.addAttribute("tasks", taskService.getAllTasks("id", null));
                model.addAttribute("users", userService.allUsers());
                model.addAttribute("currentSort", "id");
                model.addAttribute("search", "");

                return "all_tasks";
            }

            Task savedTask = taskService.createTask(task,currentUser);

            if (file != null && !file.isEmpty()) {
                taskAttachmentService.addAttachment( file,savedTask.getId());
            }

            log.info("Задача ID {} успешно создана", savedTask.getId());
            return "redirect:/all-tasks";
        }finally {
            clearMDC();
        }
    }

        @PostMapping("/all-tasks/update")
        public String updateTask(@Valid Task task,
                                 BindingResult bindingResult,
                                 @RequestParam(value = "returnUrl", required = false) String returnUrl,
                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                 @RequestParam(defaultValue = "id_asc") String sort,
                                 Model model) throws IOException {
            addUserToMDC();
            String finalRedirect = (returnUrl != null && !returnUrl.isEmpty()) ? returnUrl : "/all-tasks";
            try {
                if (bindingResult.hasErrors()) {
                    log.warn("Ошибки валидации при обновлении задачи ID {}: {}", task.getId(), bindingResult.getAllErrors());
                    return "redirect:" + finalRedirect + (finalRedirect.contains("?") ? "&" : "?") + "error=validation";
                }

                User currentUser = getCurrentUser();

                String originalName = null;
                String storedName = null;

                return "redirect:" + finalRedirect;
            }
            catch (Exception e){
                log.error("Ошибка при обновлении задачи {}",e.getMessage(),e);
                return "redirect:" + finalRedirect + (finalRedirect.contains("?") ? "&" : "?") + "error=true";
            }
            finally {
                clearMDC();
            }
        }

    @PostMapping("/all-tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, @RequestParam(defaultValue = "id_asc") String sort){
        addUserToMDC();
        try {
            User currentUser = getCurrentUser();
            log.info("Удаление задачи");
            taskService.deleteTask(id,currentUser);
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
    public String userTasks(@RequestParam(defaultValue = "id_asc") String sort,
                            @RequestParam(required = false) String search,
                            Model model){
        User currentUser = getCurrentUser();
        if (currentUser == null) return "redirect:/authorization";
        addUserToMDC();
        try {
            log.info("Запрос задачей пользователя {}",getCurrentUserEmail());

            List<Task> tasks = taskService.getAllUserTasks(currentUser.getId(),sort,search);
            List<User> allUsers = userService.allUsers();

            model.addAttribute("tasks", tasks);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("currentSort",sort);
            model.addAttribute("search",search);
            model.addAttribute("users", allUsers);
            return "user_tasks";
        }
        finally {
            clearMDC();
        }
    }
}
