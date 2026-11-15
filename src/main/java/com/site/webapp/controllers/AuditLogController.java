package com.site.webapp.controllers;

import com.site.webapp.dto.AuditLogDto;
import com.site.webapp.security.CustomUserDetails;
import com.site.webapp.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/audit")
public class AuditLogController extends LoggingController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * История действий.
     * Обычный пользователь видит только свои действия; администратор — все.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String history(@AuthenticationPrincipal CustomUserDetails user,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          Model model) {
        if (user == null) {
            return "redirect:/authorization";
        }

        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<AuditLogDto> history = isAdmin
                ? auditLogService.getAllHistory(pageable)
                : auditLogService.getActorHistory(user.getId(), pageable);

        model.addAttribute("auditLogs", history);
        model.addAttribute("isAdminView", isAdmin);
        return "audit";
    }
}