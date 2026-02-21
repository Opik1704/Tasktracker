package com.site.webapp.controllers;

import com.site.webapp.models.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Главная страница");
        return "home";
    }
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal User currentUser, Model model){
        model.addAttribute("user",currentUser);
        return "profile";
    }
}