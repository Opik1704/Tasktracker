package com.site.webapp.dto;

import com.site.webapp.models.AuditLog;

import java.time.LocalDateTime;

/**
 * DTO записи аудита для передачи на клиент.
 * Не содержит JPA-сущностей.
 */
public class AuditLogDto {

    private Long id;
    private Long actorId;
    private String actorEmail;
    private String actionType;
    private String entityType;
    private Long entityId;
    private String description;
    private LocalDateTime createdAt;

    public AuditLogDto() {
    }

    public AuditLogDto(Long id, Long actorId, String actorEmail, String actionType, String entityType,
                       Long entityId, String description, LocalDateTime createdAt) {
        this.id = id;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static AuditLogDto from(AuditLog auditLog) {
        return new AuditLogDto(
                auditLog.getId(),
                auditLog.getActorId(),
                auditLog.getActorEmail(),
                auditLog.getActionType(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDescription(),
                auditLog.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getActorId() {
        return actorId;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public String getActionType() {
        return actionType;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}