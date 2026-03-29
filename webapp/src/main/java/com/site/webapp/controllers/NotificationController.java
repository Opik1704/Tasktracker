package com.site.webapp.controllers;

import com.site.webapp.models.User;
import com.site.webapp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
public class NotificationController extends LoggingController {

    @Autowired
    private NotificationService notificationService;

        @PostMapping("/notifications/mark-all-read")
    public String markAllAsRead(@RequestHeader(value = "referer", required = false) String referer) {
        User user = getCurrentUser();
        if (user != null) {
            notificationService.markAllAsRead(user);
        }

        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
}
