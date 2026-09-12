package com.site.webapp.controllers;

import com.site.webapp.security.CustomUserDetails;
import com.site.webapp.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notifications")
public class NotificationController extends LoggingController {

    private final NotificationService notificationService;
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/mark-all-read")
    public String markAllAsRead(@AuthenticationPrincipal CustomUserDetails user,
                                @RequestHeader(value = "referer", required = false) String referer) {
        if (user != null) {
            notificationService.markAllAsRead(user.getId());
        }

        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
}
