package com.site.webapp.exception;

public class RoleNotFoundExceptionException extends EntityNotFoundException {
    public RoleNotFoundExceptionException(Long roleId){
        super("Роль с ID " + roleId + " не найдена");
    }
    public RoleNotFoundExceptionException(String message){
        super(message);
    }
}
