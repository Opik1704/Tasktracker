package com.site.webapp.interceptor;

import com.site.webapp.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class MdcLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){

        String traceId = UUID.randomUUID().toString().substring(0,8);
        MDC.put("traceId", traceId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            MDC.put("userId", user.getId().toString());
            MDC.put("userEmail", user.getEmail());
        }
        return true;
    }

    @Override
    public void afterCompletion( HttpServletRequest request,HttpServletResponse response, Object handler, Exception ex){
        MDC.clear();
    }

}
