package com.site.webapp.controllers;

import com.site.webapp.dto.ChangePasswordDto;
import com.site.webapp.dto.ChangeProfileDto;
import com.site.webapp.models.User;
import com.site.webapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController extends LoggingController {
    private final UserService userService;
    public ProfileController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public String profile(@AuthenticationPrincipal User currentUser, Model model) {

    if (currentUser == null) return "redirect:/authorization";
        User freshUser = userService.findUserById(currentUser.getId());

        log.info("Пользователь {} открыл свой профиль", getCurrentUserEmail());
        model.addAttribute("user", freshUser);

        return "profile";
    }

    @PostMapping("/update-info")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                                @Valid @ModelAttribute ChangeProfileDto changeProfileDto,
                                BindingResult bindingResult) {
        if (currentUser == null) return "redirect:/authorization";

        log.info("Пользователь {} обновляет данные", currentUser.getEmail());
        userService.updateUserInfo(currentUser.getId(),changeProfileDto.getFirstName(),changeProfileDto.getLastName(),changeProfileDto.getVersion());
        return "redirect:/profile";

    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal User currentUser,
                                 @Valid @ModelAttribute ChangePasswordDto changePasswordDto) {
        if (currentUser == null) return "redirect:/authorization";

        String result = userService.updatePassword(
                currentUser.getId(),
                changePasswordDto.getOldPassword(),
                changePasswordDto.getNewPassword(),
                changePasswordDto.getConfirmPassword()
        );

        if ("success".equals(result)) {
            return "redirect:/profile?passwordSuccess";
        } else {
            return "redirect:/profile?" + result;
        }
    }

    @PostMapping("/update-avatar")
    public String updateAvatar(@AuthenticationPrincipal User currentUser,
                               @RequestParam("avatar") MultipartFile file,
                               RedirectAttributes redirectAttributes){

        if (currentUser == null) return "redirect:/authorization";

        log.info("Пользователь {} обновляет аватарку", currentUser.getEmail());

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Файл не выбран");
            return "redirect:/profile";
        }

        userService.updateAvatar(currentUser.getId(), file);

        redirectAttributes.addFlashAttribute("success", "Аватарка успешно обновлена!");
        return "redirect:/profile";

    }

}
