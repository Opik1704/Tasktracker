package com.site.webapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordDto {
    @NotBlank(message =  "Введите Старый пароль")
    private String oldPassword;

    @NotBlank(message =  "Введите новый пароль")
    @Size(min = 4, message = "Пароль должен содержать не менее 4 символов")
    private String newPassword;

    @NotBlank(message =  "Подтвердите новый пароль")
    private String confirmPassword;

    public String getOldPassword() {
        return oldPassword;
    }
    public void setOldPassword(String oldPassword){
        this.oldPassword = oldPassword;
    }
    public void setNewPassword(String newPassword){
        this.newPassword = newPassword;
    }
    public String getNewPassword() {
        return newPassword;
    }
    public String getConfirmPassword(){
        return confirmPassword;
    }
    public void setConfirmPassword(String confirmPassword){
        this.confirmPassword = confirmPassword;
    }
}
