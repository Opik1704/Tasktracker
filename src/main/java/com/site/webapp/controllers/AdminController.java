package com.site.webapp.controllers;

import com.site.webapp.models.Role;
import com.site.webapp.models.User;
import com.site.webapp.security.CustomUserDetails;
import com.site.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController extends LoggingController{

    private final UserService userService;
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String userList(Model model) {
        log.info("Администратор {} открыл панель управления",getCurrentUserEmail());

        List<User> users = userService.allUsers();
        List<Role> allRoles = userService.getAllRoles();
        log.debug("Найдено {} пользователей",users.size());

        model.addAttribute("users", users);
        model.addAttribute("allRoles", allRoles);
        return "admin";

    }

    @PostMapping("/update-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUsersRole(@RequestParam Long userId,
                                @RequestParam(required = false) List<Long> roleIds,
                                @RequestParam(required = false) Long version,
                                @AuthenticationPrincipal CustomUserDetails currentAdmin,
                                RedirectAttributes redirectAttributes){
        log.info("Администратор {} изменяет роли пользователя ID: {}", getCurrentUserEmail(), userId);

        userService.updateUserRoles(userId,roleIds,version);

        redirectAttributes.addFlashAttribute("success", "Роли пользователя ID " + userId + " обновлены");
        log.info("Роли пользователя ID {} обновлены", userId);
        return "redirect:/admin";

    }

    @PostMapping("/soft-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String softDeleteUser(@RequestParam Long userId,@AuthenticationPrincipal CustomUserDetails currentAdmin) {
        log.info("Администратор {} удаляет пользователя ID: {}", currentAdmin.getUsername(), userId);
        userService.softDeleteUser(userId, currentAdmin);
        return "redirect:/admin";
    }

    @PostMapping("/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public String archiveUser(@RequestParam Long userId,@AuthenticationPrincipal CustomUserDetails currentAdmin){
        log.info("Администратор {} архивирует пользователя ID: {}", currentAdmin.getUsername(), userId);
        userService.archiveUser(userId, currentAdmin);
        return "redirect:/admin";
    }

}
