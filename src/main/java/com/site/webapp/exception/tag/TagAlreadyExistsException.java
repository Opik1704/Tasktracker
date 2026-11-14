package com.site.webapp.exception.tag;

import com.site.webapp.exception.DomainException;

public class TagAlreadyExistsException extends DomainException {
    public TagAlreadyExistsException(String message) {
        super(message);
    }
    public TagAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}