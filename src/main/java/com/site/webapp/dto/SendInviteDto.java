package com.site.webapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO для отправки приглашения на регистрацию.
 * Содержит email пользователя и роль, которая будет назначена при регистрации.
 * Email должен быть уникален и валиден.
 */

public class SendInviteDto {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Укажите роль для пользователя")
    private String roleName;

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
