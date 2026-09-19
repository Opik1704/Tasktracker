package com.site.webapp.controllers;

import com.site.webapp.dto.AcceptInviteDto;
import com.site.webapp.dto.RegistrationDto;
import com.site.webapp.models.InviteToken;
import com.site.webapp.service.RegistrationService;
import com.site.webapp.service.UserService;
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

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public String registration(Model model){
        log.info("Открыта страница регистрации");
        model.addAttribute("userForm",new RegistrationDto());
        return "registration";
    }

    @PostMapping
    public String standartRegistration(@Valid @ModelAttribute("userForm") RegistrationDto registrationDto, BindingResult bindingResult){

        if (registrationDto.getPassword() != null && !registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.userForm", "Пароли не совпадают");
        }

        if(bindingResult.hasErrors()){
            log.warn("Ошибки валидации при стандартной регистрации: {}", bindingResult.getAllErrors());
            return "registration";
        }

        registrationService.registerStandartUser(registrationDto);

        log.info("Пользователь {} успешно зарегистрирован", registrationDto.getEmail());

        return "redirect:/authorization?registered=true";
    }


    @PostMapping("/invite-token")
    public String inviteRegistration(@Valid @ModelAttribute("inviteForm") AcceptInviteDto dto, BindingResult bindingResult){
        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.inviteForm", "Пароли не совпадают");
        }
        if(bindingResult.hasErrors()){
            log.warn("Ошибки валидации при регистрации по приглашению: {}", bindingResult.getAllErrors());
            return "registration";
        }

        registrationService.registerByInviteToken(dto.getToken(),dto);

        log.info("Пользователь {} успешно зарегистрирован", dto.getFirstName()+dto.getLastName());
        return "redirect:/authorization?registered=true";
    }


}
