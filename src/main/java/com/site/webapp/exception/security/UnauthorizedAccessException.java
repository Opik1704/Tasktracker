package com.site.webapp.exception.security;

public class UnauthorizedAccessException extends AccessDeniedException{
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
