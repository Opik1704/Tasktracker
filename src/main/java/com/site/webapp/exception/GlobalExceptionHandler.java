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
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String handleOptimisticLock(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.warn("Конфликт одновременно редактируемых данных по адресу: {}", request.getRequestURI());
        redirectAttributes.addFlashAttribute("errorMessage", "Данные были изменены другим пользователем. Попробуйте снова.");

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/all-tasks");
    }

    @ExceptionHandler(FileStorageException.class)
    public String handleFileStorageException(FileStorageException e, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.error("Ошибка при работе с файлом: {}", e.getMessage(), e);
        redirectAttributes.addFlashAttribute("errorMessage", "Не удалось обработать файл: " + e.getMessage());

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/all-tasks");
    }

    @ExceptionHandler(Exception.class)
    public String handleUserNotFoundException(UserNotFoundException e,HttpServletRequest request,RedirectAttributes redirectAttributes){
        log.error("User not found id: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage","Не удалось найть пользователя" + e.getMessage());

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/all-tasks");
    }

    @ExceptionHandler(Exception.class)
    public String handleTaskNotFoundException(TaskNotFoundException e,HttpServletRequest request, RedirectAttributes redirectAttributes){
        log.error("Task not found {}", e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage","Не удалось найти задачу" + e.getMessage());

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/all-tasks");
    }

    @ExceptionHandler(Exception.class)
    public String handleRoleNotFoundException(RoleNotFoundException e, HttpServletRequest request, RedirectAttributes redirectAttributes){
        log.error("Role not found {}", e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage","Не удалось найти роль" + e.getMessage());

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/all-tasks");
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e) {
        log.error("Unexpected error", e);
        return "error";
    }
}
