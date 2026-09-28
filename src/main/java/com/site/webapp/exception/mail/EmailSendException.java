package com.site.webapp.exception.mail;

import com.site.webapp.exception.DomainException;

public class EmailSendException extends DomainException {
    public EmailSendException(String message) {
        super(message);
    }

    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
