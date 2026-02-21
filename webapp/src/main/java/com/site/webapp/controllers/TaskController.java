package com.site.webapp.controllers;

import com.site.webapp.models.Tasks;
import com.site.webapp.models.User;
import com.site.webapp.repo.TasksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import com.site.webapp.service.UserService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class TaskController{
    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/all_tasks")
    public String allTasks(Model model){
        Iterable<Tasks> tasks = tasksRepository.findAll();
        List<User> users = userService.allUsers();
        model.addAttribute("tasks",tasks);
        model.addAttribute("users",users);
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
    @GetMapping("/user_tasks")
    public String userTasks(@AuthenticationPrincipal User currentUser, Model model){
        Iterable<Tasks> tasks = tasksRepository.findByArtistId(currentUser.getId());
        List<User> users = userService.allUsers();
        model.addAttribute("tasks",tasks);
        model.addAttribute("users",users);
        return "user_tasks";
    }

//    @GetMapping("/user_tasks")
//    public String userTasks(Model model){
//        Iterable<Tasks> tasks = tasksRepository.findAllById(user.id);
//        model.addAttribute("tasks",tasks);
//        return "user_tasks";
//    }

}
