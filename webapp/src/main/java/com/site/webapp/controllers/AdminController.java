package com.site.webapp.controllers;

import com.site.webapp.models.Role;
import com.site.webapp.models.User;
import com.site.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController extends LoggingController{

    @Autowired
    private UserService userService;

    @GetMapping("/admin")
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
    public String deleteUser(@RequestParam Long userId) {
        addUserToMDC();
        try {
            log.info("Администратор {} удаляет пользователя ID: {}", getCurrentUserEmail(), userId);
            User user = userService.findUserById(userId);
            if (user != null) {
                log.debug("Удаляемый пользователь: {} {}", user.getFirstName(), user.getLastName());
                userService.deleteUser(userId);
                log.info("Пользователь if {} успешно удален", userId);
            } else {
                log.warn("Пользователь if {} не найден", userId);
            }
            return "redirect:/admin";
        }
        finally {
            clearMDC();
        }
    }
    @PostMapping("/edit")
    public String editUsersRole(@RequestParam Long userId, @RequestParam(required = false) List<Long> roleIds){
        addUserToMDC();
        try {
            log.info("Администратор {} изменяет роли пользователя ID: {}", getCurrentUserEmail(), userId);
            log.debug("Новые роли: {}", roleIds);
            userService.updateUserRoles(userId,roleIds);
            log.info("✅ Роли пользователя ID {} обновлены", userId);
            return "redirect:/admin";
        }
        finally {
            clearMDC();
        }
    }
}
