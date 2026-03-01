package com.site.webapp.controllers;

import com.site.webapp.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class LoggingController {
    private static final Logger log = LoggerFactory.getLogger(getClass());
    protected User getCurrentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.getPrincipal() instanceof User){
            return (User) auth.getPrincipal();
        }
        return null;
    }
    protected Long getCurrentId(){
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
    protected String getCurrentUserEmail(){
        User user = getCurrentUser();
        return user != null ? user.getEmail() : "anonim";
    }
    protected void addUserToMDC() {
        User user = getCurrentUser();
        if (user != null) {
            MDC.put("userId", user.getId().toString());
            MDC.put("userEmail", user.getEmail());
        }
    }
    protected void clearMDC() {
        MDC.clear();
    }
}
