package com.site.webapp.exception.team;

import com.site.webapp.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TeamAccessDeniedException extends DomainException {

    public TeamAccessDeniedException(Long teamId) {
        super("Доступ к команде запрещен");
    }

    public TeamAccessDeniedException(String message) {
        super(message);
    }
}
