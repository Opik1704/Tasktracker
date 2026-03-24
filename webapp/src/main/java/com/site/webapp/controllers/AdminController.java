package com.site.webapp.controllers;

import com.site.webapp.models.Role;
import com.site.webapp.models.User;
import com.site.webapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController extends LoggingController{

    @Autowired
    private UserService userService;

    @GetMapping
    public String userList(Model model) {
        addUserToMDC();
        try{
            log.info("Администратор {} открыл панель управления",getCurrentUserEmail());

            List<User> users = userService.allUsers();
            List<Role> allRoles = userService.getAllRoles();
            log.debug("Найдено {} пользователей",users.size());

            model.addAttribute("users", users);
            model.addAttribute("allRoles", allRoles);
            return "admin";
        }
        finally {
            clearMDC();
        }
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam Long userId,@AuthenticationPrincipal User currentAdmin) {
        addUserToMDC();
        try {
            log.info("Администратор {} удаляет пользователя ID: {}", getCurrentUserEmail(), userId);
            boolean deleted = userService.deleteUser(userId, currentAdmin.getId(),currentAdmin.getEmail());
            if (!deleted) {
                log.warn("Не удалось удалить пользователя ID: {}", userId);
            }
            return "redirect:/admin";
        }
        finally {
            clearMDC();
        }
    }
    @PostMapping("/update-roles")
    public String editUsersRole(@RequestParam Long userId, @RequestParam(required = false) List<Long> roleIds, HttpServletRequest request){
        addUserToMDC();
        try {
            log.info("Администратор {} изменяет роли пользователя ID: {}", getCurrentUserEmail(), userId);
            userService.updateUserRoles(userId,roleIds);
            log.info("✅ Роли пользователя ID {} обновлены", userId);
            return "redirect:/admin";
        }
        finally {
            clearMDC();
        }
    }
}
