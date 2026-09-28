package com.site.webapp.exception.base;

import com.site.webapp.exception.DomainException;

public abstract class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String message) {
        super(message);
    }
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
