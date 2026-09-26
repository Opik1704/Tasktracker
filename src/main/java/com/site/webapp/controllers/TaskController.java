package com.site.webapp.controllers;

import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.security.CustomUserDetails;
import com.site.webapp.service.TaskAttachmentService;
import com.site.webapp.service.TaskService;
import com.site.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
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
                           @AuthenticationPrincipal CustomUserDetails currentUser,
                           Model model){

        log.info("Пользователь,запросил список всех задач");
        log.debug("Параметры: sort = {}",sort);

        List<Task> tasks = taskService.getAllTasks(sort,search);

        model.addAttribute("tasks", tasks);
        model.addAttribute("users",userService.allUsers());
        model.addAttribute("currentSort", sort);
        model.addAttribute("search",search);
        model.addAttribute("currentUser", currentUser);

        return "all_tasks";

    }

    @PostMapping("/all-tasks")
    public String addTask(@Valid Task task,
                          BindingResult bindingResult,
                          @RequestParam(value = "files", required = false) List<MultipartFile> files,
                          @AuthenticationPrincipal CustomUserDetails currentUser,
                          Model model) throws IOException {

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при создании задачи: {}", bindingResult.getAllErrors());
            model.addAttribute("tasks", taskService.getAllTasks("id", null));
            model.addAttribute("users", userService.allUsers());
            model.addAttribute("currentSort", "id");
            model.addAttribute("search", "");

            return "all_tasks";
        }

        Task savedTask = taskService.createTask(task,currentUser);

        if (files != null && !files.isEmpty()) {
            for(MultipartFile file : files){
                if (!file.isEmpty()) {
                    taskAttachmentService.uploadAttachment(file, savedTask.getId());
                }
            }
        }

        log.info("Задача ID {} успешно создана", savedTask.getId());
        return "redirect:/all-tasks";

    }

    @GetMapping("/all-tasks/check-workload/{userId}")
    public String checkArtistLoad(@PathVariable Long userId, Model model) {
        long activeTasks = userService.getActiveTaskCount(userId);
        if (activeTasks >= 5) { // ваш лимит
            model.addAttribute("warning", "Внимание: у исполнителя уже " + activeTasks + " активных задач");
        }
        return "all_tasks :: warning";

    }

    @PostMapping("/all-tasks/update")
    public String updateTask(@Valid Task task,
                             BindingResult bindingResult,
                             @RequestParam(value = "returnUrl", required = false) String returnUrl,
                             @RequestParam(value = "files", required = false) List<MultipartFile> files,
                             @RequestParam(defaultValue = "id_asc") String sort,
                             @AuthenticationPrincipal CustomUserDetails currentUser,
                             Model model) throws IOException {

        String finalRedirect = (returnUrl != null && !returnUrl.isEmpty()) ? returnUrl : "/all-tasks";

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при обновлении задачи ID {}: {}", task.getId(), bindingResult.getAllErrors());
            return "redirect:" + finalRedirect + (finalRedirect.contains("?") ? "&" : "?") + "error=validation";
        }

        taskService.updateTask(task, currentUser);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    taskAttachmentService.uploadAttachment(file, task.getId());
                }
            }
        }

        log.info("Задача ID {} успешно обновлена пользователем {}", task.getId(), currentUser.getUsername());
        return "redirect:" + finalRedirect;

    }

    @PostMapping("/all-tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id,
                             @RequestParam(value = "returnUrl", required = false) String returnUrl,
                             @RequestParam(defaultValue = "id_asc") String sort,
                             @AuthenticationPrincipal CustomUserDetails currentUser
    ){
        log.info("Пользователь {} перемещает задачу ID {} в корзину", currentUser.getUsername(), id);
        taskService.softDeleteTask(id, currentUser);

        if (returnUrl != null && returnUrl.startsWith("/")) {
            return "redirect:" + returnUrl;
        }

        return "redirect:/all-tasks?sort=" + sort;
    }

    @GetMapping("/trash")
    public String trash(
                        @AuthenticationPrincipal CustomUserDetails currentUser,
                        Model model){
        if (currentUser == null) return "redirect:/authorization";
        log.info("Пользователь {} открыл корзину", currentUser.getUsername());
        List<Task> trashTasks = taskService.getTrashTasks(currentUser.getId(), currentUser);

        model.addAttribute("tasks", trashTasks);
        model.addAttribute("users", userService.allUsers());
        model.addAttribute("currentUser", currentUser);

        return "trash";
    }

    @PostMapping("/trash/restore/{id}")
    public String restoreTask(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails currentUser){
        if (currentUser == null) return "redirect:/authorization";
        log.info("Восстановление задачи ID {} из корзины пользователем {}", id, currentUser.getUsername());
        taskService.restoreTask(id, currentUser);
        return "redirect:/trash";
    }

    @PostMapping("/trash/hard-delete/{id}")
    public String hardDeleteTask(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.info("Окончательное удаление задачи ID {} пользователем {}", id, currentUser.getUsername());
        taskService.hardDeleteTask(id, currentUser);

        return "redirect:/trash";
    }

    @GetMapping("/favorites")
    public String favorites(@RequestParam(required = false) String sort,
                            @AuthenticationPrincipal CustomUserDetails currentUser,
                            Model model) {
        if (currentUser == null) return "redirect:/authorization";

        log.info("Пользователь {} просматривает избранное (сортировка: {})", currentUser.getUsername(), sort);

        model.addAttribute("tasks", taskService.getSortedFavorites(currentUser.getUsername(), sort));
        model.addAttribute("users", userService.allUsers());
        model.addAttribute("pageTitle", "Избранные задачи");

        return "favorites";
    }

    @PostMapping("/favorites/toggle/{taskId}")
    public String toggleFavorite(@PathVariable Long taskId,
                                 @AuthenticationPrincipal CustomUserDetails currentUser,
                                 HttpServletRequest request) {
        if (currentUser == null) return "redirect:/authorization";


        log.info("Пользователь {} переключает избранное для задачи {}",currentUser.getUsername(), taskId);
        taskService.toggleFavorite(currentUser.getUsername(),taskId);

        String referer = request.getHeader("Referer");

        if (referer != null) {
            return "redirect:" + referer;
        }

        return "redirect:/all-tasks";
    }

    @GetMapping("/user-tasks")
    public String userTasks(@RequestParam(defaultValue = "id_asc") String sort,
                            @RequestParam(required = false) String search,
                            @AuthenticationPrincipal CustomUserDetails currentUser,
                            Model model){

        if (currentUser == null) return "redirect:/authorization";

        log.info("Запрос задачей пользователя {}",currentUser.getUsername());

        List<Task> tasks = taskService.getAllUserTasks(currentUser.getId(),sort,search);
        List<User> allUsers = userService.allUsers();

        model.addAttribute("tasks", tasks);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentSort",sort);
        model.addAttribute("search",search);
        model.addAttribute("users", allUsers);
        return "user_tasks";

    }
}
