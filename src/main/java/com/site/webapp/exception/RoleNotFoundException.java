package com.site.webapp.exception;

public class RoleNotFoundException extends EntityNotFoundException {
    public RoleNotFoundException(Long roleId){
        super("Роль с ID " + roleId + " не найдена");
    }
    public RoleNotFoundException(String message){
        super(message);
    }
}
