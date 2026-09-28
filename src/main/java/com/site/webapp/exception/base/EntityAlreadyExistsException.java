package com.site.webapp.exception.base;

import com.site.webapp.exception.DomainException;

public class EntityAlreadyExistsException extends DomainException {
    public EntityAlreadyExistsException(String message) {
        super(message);
    }
    public EntityAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
