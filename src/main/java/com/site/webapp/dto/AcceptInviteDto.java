package com.site.webapp.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для принятия приглашения и регистрации.
 * Используется при регистрации по токену приглашения.
 * Валидация:
 * - Токен: обязателен
 * - Имя и фамилия: 1-50 символов
 * - Пароль: минимум 4 символа
 * - Пароль и подтверждение должны совпадать
 */

public class AcceptInviteDto{

        @NotBlank(message = "Токен обязателен")
        String token;

        @NotBlank(message = "Имя обязательно")
        @Size(min = 1,max = 50,message = "Имя должно содержать от 1 до 50 символов")
        String firstName;

        @NotBlank(message = "Фамилия обязательна")
        @Size(min = 1,max= 50,message = "Фамилия должна содержать от 1 до 50 символов" )
        String lastName;

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 4, message = "Пароль должен содержать не менее 4 символов")
        String password;

        @NotBlank(message = "Подтверждение пароля обязательно")
        private String confirmPassword;


        public String getToken() {
                return token;
        }
        public void setToken(String token) {
                this.token = token;
        }


        public String getFirstName() {
                return firstName;
        }
        public void setFirstName(String firstName) {
                this.firstName = firstName;
        }


        public String getLastName() {
                return lastName;
        }
        public void setLastName(String lastName) {
                this.lastName = lastName;
        }


        public String getPassword() {
                return password;
        }
        public void setPassword(String password) {
                this.password = password;
        }

        public String getConfirmPassword() {
                return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
                this.confirmPassword = confirmPassword;
        }
}