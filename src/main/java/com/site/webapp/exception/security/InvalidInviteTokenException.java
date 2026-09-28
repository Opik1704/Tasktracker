package com.site.webapp.exception.security;

import com.site.webapp.exception.base.BusinessRuleViolationException;

public class InvalidInviteTokenException extends BusinessRuleViolationException {
    public InvalidInviteTokenException(String message) {
        super(message);
    }
}
