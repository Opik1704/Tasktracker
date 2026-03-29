package com.site.webapp.controllers;

import com.site.webapp.models.User;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class MainController extends LoggingController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    NotificationService notificationService;

    @GetMapping("/")
    public String home(Principal principal, Model model) {
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName());
            model.addAttribute("currentUser", user);

            model.addAttribute("notificationsCount", notificationService.getUnreadCount(user.getId()));
            model.addAttribute("lastNotifications", notificationService.getLastNotifications(user.getId()));
        } else {
            model.addAttribute("currentUser", null);
            model.addAttribute("notificationsCount", 0);
            model.addAttribute("lastNotifications", new java.util.ArrayList<>());
        }

        model.addAttribute("title", "Главная");
        return "home";
    }
}