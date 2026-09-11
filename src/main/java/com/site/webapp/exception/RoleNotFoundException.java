package com.site.webapp.exception;

public class RoleNotFoundException extends RuntimeException{
    public RoleNotFoundException(Long roleId){
        super("Роль с ID " + roleId + " не найдена");
    }
    public RoleNotFoundException(String message){
        super(message);
    }
}
