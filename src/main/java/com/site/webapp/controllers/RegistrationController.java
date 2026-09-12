package com.site.webapp.controllers;

import com.site.webapp.dto.RegistrationDto;
import com.site.webapp.models.User;
import com.site.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/registration")
public class RegistrationController extends LoggingController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String registration(Model model){
        log.info("Открыта страница регистрации");

        model.addAttribute("userForm",new RegistrationDto());

        return "registration";
    }

    @PostMapping
    public String addUser(@ModelAttribute("userForm") @Valid RegistrationDto registrationDto,
                          BindingResult bindingResult,
                          Model model) {

        if (bindingResult.hasErrors()) {
            return "registration";
        }

        String result = userService.registerNewUser(registrationDto);

        if ("passwordError".equals(result)) {
            model.addAttribute("passwordError", "Пароли не совпадают");
            return "registration";
        }

        if ("emailError".equals(result)) {
            model.addAttribute("emailError", "Пользователь с таким email уже существует");
            return "registration";
        }

        return "redirect:/authorization?registered=true";
    }
}
