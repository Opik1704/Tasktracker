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

@Controller
public class RegistrationController extends LoggingController {
    @Autowired
    private UserService userService;

    @GetMapping("/registration")
    public String registration(Model model){
        log.info("Открыта страница регистрации");
        model.addAttribute("userForm",new RegistrationDto());
        return "registration";
    }
    @PostMapping("/registration")
    public String addUser(@ModelAttribute("userForm") @Valid RegistrationDto registrationDto, BindingResult bindingResult, Model model) {
        log.info("Регистрация пользователя с email {}",registrationDto.getEmail());
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка валидации при регистрации {}",bindingResult.getAllErrors());
            return "registration";
        }
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            log.warn("Пароли не совпадают для email {}",registrationDto.getEmail());
            model.addAttribute("passwordError", "Пароли не совпадают");
            return "registration";
        }

        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(registrationDto.getPassword());

        if (!userService.saveUser(user)) {
            log.warn("Регистрация не удалась email {} уже существует", registrationDto.getEmail());
            model.addAttribute("emailError", "Пользователь с таким email уже существует");
            return "registration";
        }
        log.info("Пользователь {} зарегестрирован с id {}",registrationDto.getEmail(),user.getId());
        return "redirect:/authorization?registered=true";
    }
}
