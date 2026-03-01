package com.site.webapp.controllers;

import com.site.webapp.dto.RegistrationDto;
import com.site.webapp.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class RegistrationControllerTest {
    @Autowired
    private RegistrationController registrationController;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registration() {
        Model model = new org.springframework.ui.ExtendedModelMap();
        String viewName = registrationController.registration(model);
        assertEquals("registration",viewName);
        assertNotNull(model.getAttribute("userForm"));
    }
    @Test
    void addUser() {
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("Testov");
        registrationDto.setEmail("testemail@mail.ru");
        registrationDto.setPassword("password");
        registrationDto.setConfirmPassword("password");

        BindingResult bindingResult = new BeanPropertyBindingResult(registrationDto, "userForm");
        Model model = new org.springframework.ui.ExtendedModelMap();

        String result = registrationController.addUser(registrationDto,bindingResult,model);

        assertEquals("redirect:/authorization?registered=true", result);
        assertNotNull(userRepository.findByEmail("testemail@mail.ru"));
    }
    @Test
    void passwordDismatch(){
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEmail("testemail@mail.ru");
        registrationDto.setPassword("123456");
        registrationDto.setConfirmPassword("654321");

        BindingResult bindingResult = new BeanPropertyBindingResult(registrationDto, "userForm");
        Model model = new org.springframework.ui.ExtendedModelMap();
        String result = registrationController.addUser(registrationDto, bindingResult, model);

        assertEquals("registration", result);
        assertNotNull(model.getAttribute("passwordError"));
        assertNull(userRepository.findByEmail("testemail@mail.ru"));
    }
}