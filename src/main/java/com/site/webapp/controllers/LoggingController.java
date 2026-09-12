package com.site.webapp.controllers;


import com.site.webapp.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class LoggingController {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected CustomUserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            return user;
        }
        return null;
    }

    protected Long getCurrentUserId() {
        CustomUserDetails user = getCurrentUserDetails();
        return user != null ? user.getId() : null;
    }

    protected String getCurrentUserEmail() {
        CustomUserDetails user = getCurrentUserDetails();
        return user != null ? user.getUsername() : "anonym";
    }

//    protected void addUserToMDC() {
//        User user = getCurrentUser();
//        if (user != null) {
//            MDC.put("userId", user.getId().toString());
//            MDC.put("userEmail", user.getEmail());
//        }
//    }
//    protected void clearMDC() {
//        MDC.clear();
//    }
}
