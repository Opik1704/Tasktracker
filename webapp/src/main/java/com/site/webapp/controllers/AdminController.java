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
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/admin")
    public String userList(Model model) {
        List<User> users = userService.allUsers();
        List<Role> allRoles = userService.getAllRoles();
        model.addAttribute("users",users);
        model.addAttribute("allRoles",allRoles);
        return "admin";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam Long userId) {
        userService.deleteUser(userId);
        return "redirect:/admin";
    }
    @PostMapping("/edit")
    public String editUsersRole(@RequestParam Long userId,
                                @RequestParam(required = false) List<Long> roleIds){
        userService.updateUserRoles(userId,roleIds);
        return "redirect:/admin";
    }
}
