package com.site.webapp.controllers;

import com.site.webapp.dto.TaskAnalyticsDto;
import com.site.webapp.security.CustomUserDetails;
import com.site.webapp.service.TaskService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController extends LoggingController {

    private static final ZoneId MOSCOW = ZoneId.of("Europe/Moscow");

    private final TaskService taskService;

    public AnalyticsController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Аналитическая панель задач пользователя с фильтром по периоду.
     * По умолчанию — последние 30 дней (сегодня включительно).
     */
    @GetMapping
    public String analytics(@AuthenticationPrincipal CustomUserDetails user,
                            @RequestParam(required = false) LocalDate from,
                            @RequestParam(required = false) LocalDate to,
                            Model model) {
        if (user == null) {
            return "redirect:/authorization";
        }

        ZonedDateTime nowMoscow = ZonedDateTime.now(MOSCOW);
        LocalDate end = to != null ? to : nowMoscow.toLocalDate();
        LocalDate start = from != null ? from : end.minusDays(29);

        // Полуоткрытый интервал [start 00:00, end+1 00:00)
        LocalDateTime periodStart = start.atStartOfDay();
        LocalDateTime periodEnd = end.plusDays(1).atStartOfDay();

        TaskAnalyticsDto analytics = taskService.getAnalytics(user.getId(), periodStart, periodEnd);

        model.addAttribute("analytics", analytics);
        model.addAttribute("periodStart", start);
        model.addAttribute("periodEnd", end);
        return "analytics";
    }
}