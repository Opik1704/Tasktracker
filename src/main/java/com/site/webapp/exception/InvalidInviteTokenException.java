package com.site.webapp.exception;

public class InvalidInviteTokenException extends RuntimeException{
    public InvalidInviteTokenException(String message) {
        super(message);
    }
}
