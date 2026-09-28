package com.site.webapp.exception.user;

import com.site.webapp.exception.base.BusinessRuleViolationException;

public class SelfDeleteException extends BusinessRuleViolationException {
    public SelfDeleteException(String message) {
        super(message);
    }
}
