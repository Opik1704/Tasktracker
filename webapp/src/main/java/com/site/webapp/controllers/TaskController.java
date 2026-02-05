package com.site.webapp.controllers;

import com.site.webapp.models.Tasks;
import com.site.webapp.repo.TasksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class TaskController{
    @Autowired
    private TasksRepository tasksRepository;
    @GetMapping("/all_tasks")
    public String allTasks(Model model){
        Iterable<Tasks> tasks = tasksRepository.findAll();
        model.addAttribute("tasks",tasks);
        return "all_tasks";
    }
    @PostMapping("/all_tasks")
    public String addTask(@RequestParam String title,
                          @RequestParam String priority,
                          @RequestParam Long artistId,
                          @RequestParam LocalDateTime deadline,
                            @RequestParam(required = false) String comment,
                          Model model){
        Tasks task = new Tasks(title,priority,artistId,deadline,comment);
        tasksRepository.save(task);
        return "redirect:/all_tasks";
    }
//    @GetMapping("/user_tasks")
//    public String userTasks(Model model){
//        Iterable<Tasks> tasks = tasksRepository.findAllById(user.id);
//        model.addAttribute("tasks",tasks);
//        return "user_tasks";
//    }

}
