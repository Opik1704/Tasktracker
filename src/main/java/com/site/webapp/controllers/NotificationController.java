package com.site.webapp.controllers;

import com.site.webapp.dto.NotificationDto;
import com.site.webapp.security.CustomUserDetails;
import com.site.webapp.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationController extends LoggingController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal CustomUserDetails user,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) Boolean read,
                       Model model) {
        if (user == null) {
            return "redirect:/authorization";
        }

        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<NotificationDto> notifications = notificationService.getNotifications(user.getId(), read, pageable);

        model.addAttribute("notifications", notifications);
        model.addAttribute("notificationsCount", notificationService.getUnreadCount(user.getId()));
        model.addAttribute("readFilter", read);
        return "notifications";
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(user.getId())));
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@AuthenticationPrincipal CustomUserDetails user,
                             @PathVariable Long id,
                             @RequestHeader(value = "referer", required = false) String referer) {
        if (user == null) {
            return "redirect:/authorization";
        }
        notificationService.markAsRead(user.getId(), id);
        return redirectBack(referer);
    }

    @PostMapping("/mark-all-read")
    public String markAllAsRead(@AuthenticationPrincipal CustomUserDetails user,
                                @RequestHeader(value = "referer", required = false) String referer) {
        if (user == null) {
            return "redirect:/authorization";
        }
        notificationService.markAllAsRead(user.getId());
        return redirectBack(referer);
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal CustomUserDetails user,
                         @PathVariable Long id,
                         @RequestHeader(value = "referer", required = false) String referer) {
        if (user == null) {
            return "redirect:/authorization";
        }
        notificationService.deleteNotification(user.getId(), id);
        return redirectBack(referer);
    }

    private String redirectBack(String referer) {
        if (isSafeRelativePath(referer)) {
            return "redirect:" + referer;
        }
        return "redirect:/notifications";
    }
}