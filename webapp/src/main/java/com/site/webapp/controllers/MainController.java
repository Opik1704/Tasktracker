package com.site.webapp.controllers;

import com.site.webapp.models.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("title", "Главная");
        return "home";
    }
}