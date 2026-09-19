package com.site.webapp.exception;

public class EntityAlreadyExistsException extends DomainException{
    public EntityAlreadyExistsException(String message) {
        super(message);
    }
    public EntityAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
