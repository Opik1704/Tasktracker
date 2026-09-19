package com.site.webapp.exception;

import com.site.webapp.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
