package com.site.webapp.controllers;

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
    public String profile(Model model){
        model.addAttribute("Username","Димка");
        return "profile";
    }
    @GetMapping("/registration")
    public String registration(Model model){
//        User user = new User();
//        user.setName("Иван Иванов");
//        user.setEmail();
//        user.setRole("Администратор");
//        model.addAttribute("user", user);
        return "registration";
    }

}