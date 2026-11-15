package com.site.webapp.service;

import com.site.webapp.dto.AuditLogDto;
import com.site.webapp.models.AuditLog;
import com.site.webapp.repo.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис журнала аудита.
 * Записывает действия пользователей и предоставляет историю с пагинацией.
 * Методы записи не бросают исключений: сбой аудита не должен ломать бизнес-операцию,
 * но запись выполняется в рамках той же транзакции, что и сама операция.
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    public static final String ACTION_TASK_CREATED = "TASK_CREATED";
    public static final String ACTION_TASK_UPDATED = "TASK_UPDATED";
    public static final String ACTION_TASK_STATUS_CHANGED = "TASK_STATUS_CHANGED";
    public static final String ACTION_TASK_DELETED = "TASK_DELETED";
    public static final String ACTION_TEAM_CREATED = "TEAM_CREATED";
    public static final String ACTION_TEAM_UPDATED = "TEAM_UPDATED";

    public static final String ENTITY_TASK = "Task";
    public static final String ENTITY_TEAM = "Team";

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Записывает событие аудита в текущую транзакцию бизнес-операции.
     * Запись атомарна с операцией: ошибка аудита откатывает и бизнес-операцию,
     * что гарантирует согласованность журнала и данных.
     */
    public void record(Long actorId, String actorEmail, String actionType, String entityType,
                       Long entityId, String description) {
        AuditLog auditLog = new AuditLog(actorId, actorEmail, actionType, entityType, entityId, truncate(description));
        auditLogRepository.save(auditLog);
        log.debug("Записано событие аудита: {} {} #{} актором {}", actionType, entityType, entityId, actorId);
    }

    /**
     * История действий пользователя с пагинацией (его собственные действия).
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getActorHistory(Long actorId, Pageable pageable) {
        return auditLogRepository.findByActorIdOrderByCreatedAtDesc(actorId, pageable)
                .map(AuditLogDto::from);
    }

    /**
     * Полная история действий с пагинацией (только для администратора).
     */
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllHistory(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AuditLogDto::from);
    }

    private String truncate(String description) {
        if (description == null) {
            return null;
        }
        return description.length() > 1000 ? description.substring(0, 1000) : description;
    }
}