package com.site.webapp.exception;

import com.site.webapp.exception.base.BusinessRuleViolationException;
import com.site.webapp.exception.base.EntityAlreadyExistsException;
import com.site.webapp.exception.mail.EmailSendException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.site.webapp.exception.base.EntityNotFoundException;
import com.site.webapp.exception.file.FileStorageException;
import com.site.webapp.exception.team.TeamAccessDeniedException;
import com.site.webapp.exception.team.TeamNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;


/**
 * Глобальный обработчик исключений.

 * Перехватывает все исключения в приложении и возвращает
 * пользователю понятные сообщения с редиректом на предыдущую страницу.
 */

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFound(EntityNotFoundException e,HttpServletRequest request, RedirectAttributes redirectAttributes){
        log.warn("Ресурс не найден {}: {}", request.getRequestURI(), e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return redirectToReferer(request, "/all-tasks");
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public String handleBusinessRuleViolation(BusinessRuleViolationException e,HttpServletRequest request,RedirectAttributes redirectAttributes){
        log.warn("Нарушение бизнес-правила [{}]: {}", request.getRequestURI(), e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return redirectToReferer(request, "/all-tasks");
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public String handleEntityAlreadyExists(EntityAlreadyExistsException e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.warn("Попытка дублирования данных [{}]: {}", request.getRequestURI(), e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return redirectToReferer(request, "/all-tasks");
}

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String handleOptimisticLock(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.warn("Конфликт одновременно редактируемых данных по адресу: {}", request.getRequestURI());
        redirectAttributes.addFlashAttribute("errorMessage", "Данные были изменены другим пользователем. Попробуйте снова.");
        return redirectToReferer(request, "/all-tasks");
    }

    @ExceptionHandler(FileStorageException.class)
    public String handleFileStorageException(FileStorageException e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.error("Ошибка при работе с файлом: {}", e.getMessage(), e);
        redirectAttributes.addFlashAttribute("errorMessage", "Не удалось обработать файл: " + e.getMessage());
        return redirectToReferer(request, "/all-tasks");
    }

    @ExceptionHandler(EmailSendException.class)
    public String handleEmailSendException(EmailSendException e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.error("Ошибка при отправке письма по адресу {}: {}", request.getRequestURI(), e.getMessage(), e);
        redirectAttributes.addFlashAttribute("errorMessage", "Не удалось отправить письмо. Попробуйте позже.");
        return redirectToReferer(request, "/all-tasks");
    }


    @ExceptionHandler(TeamAccessDeniedException.class)
    public String handleTeamAccessDenied(TeamAccessDeniedException e, HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
        log.warn("Доступ к команде запрещён [{}]: {}", request.getRequestURI(), e.getMessage());
        response.sendError(HttpStatus.FORBIDDEN.value(), e.getMessage());
        return null;
    }

    @ExceptionHandler(TeamNotFoundException.class)
    public String handleTeamNotFound(TeamNotFoundException e, HttpServletRequest request, HttpServletResponse response) throws java.io.IOException {
        log.warn("Команда не найдена [{}]: {}", request.getRequestURI(), e.getMessage());
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return null;
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, HttpServletRequest request) {
        log.error("Непредвиденная критическая ошибка по адресу: {}", request.getRequestURI(), e);
        return "error";
    }

    private String redirectToReferer(HttpServletRequest request, String fallbackPath) {
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : fallbackPath);
    }
}
