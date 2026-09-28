package com.site.webapp.exception.file;

import com.site.webapp.exception.DomainException;

public class FileUploadException extends DomainException {
    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}