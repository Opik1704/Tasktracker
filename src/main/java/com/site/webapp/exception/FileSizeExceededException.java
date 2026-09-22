package com.site.webapp.exception;

public class FileSizeExceededException extends FileUploadException{
    public FileSizeExceededException(String message){
        super(message);
    }
    public FileSizeExceededException(String message, Throwable cause){
        super(message, cause);
    }
}
