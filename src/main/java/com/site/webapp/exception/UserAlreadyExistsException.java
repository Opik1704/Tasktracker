package com.site.webapp.exception;

public class UserAlreadyExistsException extends EntityAlreadyExistsException{
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
