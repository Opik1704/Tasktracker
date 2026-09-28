package com.site.webapp.exception.file;

import com.site.webapp.exception.DomainException;

public class FileStorageException extends DomainException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
