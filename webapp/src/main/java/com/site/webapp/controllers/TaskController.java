package com.site.webapp.controllers;

import com.site.webapp.models.Tasks;
import com.site.webapp.models.User;
import com.site.webapp.repo.TasksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import com.site.webapp.service.UserService;
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
        Iterable<Tasks> tasks;
        List<User> users = userService.allUsers();
        if("deadline".equals(sort)){
            tasks = tasksRepository.findAllByOrderByDeadlineDesc();
        }
        else if("priority".equals(sort)){
            tasks = tasksRepository.findAllByOrderByPriorityAsc();
        }
        else if("id_desc".equals(sort)){
            tasks = tasksRepository.findAllByOrderByIdDesc();
        }
        else{
            tasks = tasksRepository.findAllByOrderByIdAsc();
            sort = "id_asc";
        }
        model.addAttribute("tasks",tasks);
        model.addAttribute("users",users);
        model.addAttribute("currentSort",sort);
        return "all_tasks";
    }
    @PostMapping("/all_tasks")
    public String addTask(@RequestParam String title,
                          @RequestParam String priority,
                          @RequestParam Long artistId,
                          @RequestParam  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime deadline,
                          @RequestParam(required = false) String comment,
                          Model model){
        Tasks task = new Tasks(title,priority,artistId,deadline,comment);
        tasksRepository.save(task);
        return "redirect:/all_tasks";
    }
    @PostMapping("all_tasks/update")
    public String updateTask(@RequestParam Long id,
                             @RequestParam String title,
                             @RequestParam String priority,
                             @RequestParam Long artistId,
                             @RequestParam  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime deadline,
                             @RequestParam(required = false) String comment,
                             @RequestParam(defaultValue = "id_asc") String sort,
                             Model model){
        Tasks task = tasksRepository.findById(id).orElseThrow(() -> new RuntimeException("Задача не найдена"));
        
        task.setTitle(title);
        task.setPriority(priority);
        task.setArtistId(artistId);
        task.setDeadline(deadline);
        task.setComment(comment);

        tasksRepository.save(task);
        return "redirect:/all_tasks?sort=" + sort;
    }
    @PostMapping("/all_tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id, @RequestParam(defaultValue = "id_asc") String sort){
        tasksRepository.deleteById(id);
        return "redirect:/all_tasks?sort=sort=" + sort;
    }
    @GetMapping("/user_tasks")
    public String userTasks(@AuthenticationPrincipal User currentUser, Model model){
        Iterable<Tasks> tasks = tasksRepository.findByArtistId(currentUser.getId());
        List<User> users = userService.allUsers();
        model.addAttribute("tasks",tasks);
        model.addAttribute("users",users);
        return "user_tasks";
    }
}
