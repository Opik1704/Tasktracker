package com.site.webapp.exception.user;

import com.site.webapp.exception.base.EntityAlreadyExistsException;

public class UserAlreadyExistsException extends EntityAlreadyExistsException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
