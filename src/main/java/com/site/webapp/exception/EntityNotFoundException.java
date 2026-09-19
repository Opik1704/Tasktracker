package com.site.webapp.exception;

public abstract class EntityNotFoundException extends DomainException{
    public EntityNotFoundException(String message) {
        super(message);
    }
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
