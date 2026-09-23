package com.site.webapp.exception;

public class AvatarNotFoundException extends EntityNotFoundException {
    public AvatarNotFoundException(Long userId) {
        super("Аватар не найден для пользователя ID: " + userId);
    }

    public AvatarNotFoundException(String message) {
        super(message);
    }
}