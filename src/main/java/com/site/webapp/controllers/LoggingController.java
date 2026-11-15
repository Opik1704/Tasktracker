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

    /**
     * Разрешает только относительные пути внутри приложения.
     * Защищает от open redirect: внешние URL, protocol-relative адреса,
     * javascript:, обратные слэши и управляющие символы отклоняются.
     */
    protected boolean isSafeRelativePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        if (!path.startsWith("/") || path.startsWith("//") || path.startsWith("/\\")) {
            return false;
        }
        return !path.contains("://")
                && !path.contains("\\")
                && !path.contains("\n")
                && !path.contains("\r");
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
