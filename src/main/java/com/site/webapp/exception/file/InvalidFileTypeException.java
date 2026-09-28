package com.site.webapp.exception.file;

import com.site.webapp.exception.file.FileUploadException;

public class InvalidFileTypeException extends FileUploadException {
    public InvalidFileTypeException(String message) {
        super(message);
    }
    public InvalidFileTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}