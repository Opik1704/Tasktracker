package com.site.webapp.exception;

public class SelfDeleteException extends BusinessRuleViolationException{
    public SelfDeleteException(String message) {
        super(message);
    }
}
