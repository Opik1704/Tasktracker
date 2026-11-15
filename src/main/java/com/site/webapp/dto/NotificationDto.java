package com.site.webapp.dto;

import com.site.webapp.models.Notification;

import java.time.LocalDateTime;

/**
 * DTO уведомления для передачи на клиент.
 * Не содержит JPA-сущности User и Task — только идентификатор задачи при наличии.
 */
public class NotificationDto {

    private Long id;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private Long taskId;

    public NotificationDto() {
    }

    public NotificationDto(Long id, String message, boolean read, LocalDateTime createdAt, Long taskId) {
        this.id = id;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
        this.taskId = taskId;
    }

    public static NotificationDto from(Notification notification) {
        Long taskId = notification.getTask() != null ? notification.getTask().getId() : null;
        return new NotificationDto(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                taskId
        );
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getTaskId() {
        return taskId;
    }
}