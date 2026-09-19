package com.site.webapp.exception;

public class UserNotFoundException extends EntityNotFoundException{

    public UserNotFoundException(Long userId) {
        super("Пользователь с ID " + userId + " не найден");
    }
    public UserNotFoundException(String message) {
        super(message);
    }
}