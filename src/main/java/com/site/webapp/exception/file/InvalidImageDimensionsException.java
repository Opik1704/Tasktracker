package com.site.webapp.exception.file;

import com.site.webapp.exception.file.FileUploadException;

public class InvalidImageDimensionsException extends FileUploadException {
    public InvalidImageDimensionsException(String message) {
        super(message);
    }

    public InvalidImageDimensionsException(String message, Throwable cause) {
        super(message, cause);
    }
}