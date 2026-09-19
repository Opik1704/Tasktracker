package com.site.webapp.exception;

public class InvalidPasswordException extends BusinessRuleViolationException{
    public InvalidPasswordException(String message){
        super(message);
    }
}
