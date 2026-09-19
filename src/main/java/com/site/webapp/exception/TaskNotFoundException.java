package com.site.webapp.exception;

public class TaskNotFoundException extends EntityNotFoundException{
    public TaskNotFoundException(Long taskId) {
        super("Задача с ID " + taskId + " не найдена");
    }
    public TaskNotFoundException(String message){
        super(message);
    }
}
