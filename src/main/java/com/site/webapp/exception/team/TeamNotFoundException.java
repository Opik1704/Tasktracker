package com.site.webapp.exception.team;

import com.site.webapp.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TeamNotFoundException extends DomainException {

    public TeamNotFoundException(Long teamId) {
        super("Команда не найдена");
    }

    public TeamNotFoundException(String message) {
        super(message);
    }
}
