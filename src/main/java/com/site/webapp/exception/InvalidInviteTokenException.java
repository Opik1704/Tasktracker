package com.site.webapp.exception;

public class InvalidInviteTokenException extends BusinessRuleViolationException{
    public InvalidInviteTokenException(String message) {
        super(message);
    }
}
