package com.site.webapp.exception.notification;

import com.site.webapp.exception.base.EntityNotFoundException;

public class NotificationNotFoundException extends EntityNotFoundException {
    public NotificationNotFoundException(String message) {
        super(message);
    }

    public NotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}