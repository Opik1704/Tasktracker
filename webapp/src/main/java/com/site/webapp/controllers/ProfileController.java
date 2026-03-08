package com.site.webapp.controllers;

import com.site.webapp.models.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class ProfileController extends LoggingController {
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal User currentUser, Model model){
        addUserToMDC();
        try {
            log.info("Пользователь {} открыл свой профиль",getCurrentUserEmail()):
            model.addAttribute("user", currentUser);
            return "profile";
        }
        finally {
            clearMDC();
        }
    }
    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal User currentUser, @RequestParam String firstName,@RequestParam String lastName,Model model){
        addUserToMDC();
        try {
            log.info("Пользователь");
        }
        finally {
            clearMDC();
        }
    }
}
