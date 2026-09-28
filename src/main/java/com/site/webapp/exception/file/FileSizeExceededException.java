package com.site.webapp.exception.file;

public class FileSizeExceededException extends FileUploadException {
    public FileSizeExceededException(String message){
        super(message);
    }
    public FileSizeExceededException(String message, Throwable cause){
        super(message, cause);
    }
}
