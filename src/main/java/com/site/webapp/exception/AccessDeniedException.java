package com.site.webapp.exception;

public class AccessDeniedException extends DomainException{
    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
