package com.site.webapp.exception.security;

import com.site.webapp.exception.base.BusinessRuleViolationException;

public class InvalidPasswordException extends BusinessRuleViolationException {
    public InvalidPasswordException(String message){
        super(message);
    }
}
