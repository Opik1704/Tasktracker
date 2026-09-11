package com.site.webapp.exception;

public class TaskNotFoundException extends RuntimeException{
    public TaskNotFoundException(Long taskId) {
        super("Задача с ID " + taskId + " не найдена");
    }
    public TaskNotFoundException(String message){
        super(message);
    }
}
