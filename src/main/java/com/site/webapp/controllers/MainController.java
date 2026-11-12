package com.site.webapp.controllers;

import com.site.webapp.dto.TaskStatisticsDto;
import com.site.webapp.security.CustomUserDetails;
import com.site.webapp.service.TaskService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final TaskService taskService;

    public MainController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("title", "Главная");
        if (currentUser != null) {
            TaskStatisticsDto statistics = taskService.getStatistics(currentUser.getId());
            model.addAttribute("statistics", statistics);
        }
        return "home";
    }
}