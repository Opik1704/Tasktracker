package com.site.webapp.exception.tag;

import com.site.webapp.exception.DomainException;

public class TagNotFoundException extends DomainException {
    public TagNotFoundException(String message) {
        super(message);
    }
    public TagNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}