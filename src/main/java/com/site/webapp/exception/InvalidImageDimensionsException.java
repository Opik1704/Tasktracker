package com.site.webapp.exception;

public class InvalidImageDimensionsException extends FileUploadException {
    public InvalidImageDimensionsException(String message) {
        super(message);
    }

    public InvalidImageDimensionsException(String message, Throwable cause) {
        super(message, cause);
    }
}