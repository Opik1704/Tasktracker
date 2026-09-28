package com.site.webapp.exception.base;

import com.site.webapp.exception.DomainException;

public class BusinessRuleViolationException extends DomainException {
    public BusinessRuleViolationException(String message){
        super(message);
    }
    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
