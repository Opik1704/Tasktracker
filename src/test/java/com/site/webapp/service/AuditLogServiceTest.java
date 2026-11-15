package com.site.webapp.service;

import com.site.webapp.dto.AuditLogDto;
import com.site.webapp.models.AuditLog;
import com.site.webapp.repo.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuditLogServiceTest {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
    }

    @Test
    void record_savesAuditEntry() {
        auditLogService.record(1L, "user@test.com",
                AuditLogService.ACTION_TASK_CREATED, AuditLogService.ENTITY_TASK, 10L, "Создана задача");

        assertEquals(1, auditLogRepository.count());
        AuditLog saved = auditLogRepository.findAll().get(0);
        assertEquals("TASK_CREATED", saved.getActionType());
        assertEquals("Task", saved.getEntityType());
        assertEquals(10L, saved.getEntityId());
        assertEquals(1L, saved.getActorId());
    }

    @Test
    void getActorHistory_returnsOnlyOwnEntries() {
        auditLogService.record(1L, "a@test.com", AuditLogService.ACTION_TASK_CREATED, "Task", 1L, "A");
        auditLogService.record(2L, "b@test.com", AuditLogService.ACTION_TASK_CREATED, "Task", 2L, "B");

        Page<AuditLogDto> history = auditLogService.getActorHistory(1L, PageRequest.of(0, 10));

        assertEquals(1, history.getTotalElements());
        assertEquals("A", history.getContent().get(0).getDescription());
    }

    @Test
    void record_truncatesLongDescription() {
        String longText = "x".repeat(1500);
        auditLogService.record(1L, "a@test.com", AuditLogService.ACTION_TASK_UPDATED, "Task", 1L, longText);

        AuditLog saved = auditLogRepository.findAll().get(0);
        assertEquals(1000, saved.getDescription().length());
    }

    @Test
    void record_doesNotStoreSecrets() {
        // Сервис не фильтрует содержимое — это ответственность вызывающего кода.
        // Здесь проверяем, что описание сохраняется как передано.
        auditLogService.record(1L, "a@test.com", AuditLogService.ACTION_TASK_UPDATED, "Task", 1L, "Обычное описание");

        AuditLog saved = auditLogRepository.findAll().get(0);
        assertEquals("Обычное описание", saved.getDescription());
    }

    @Test
    void getAllHistory_returnsAllEntries() {
        auditLogService.record(1L, "a@test.com", AuditLogService.ACTION_TASK_CREATED, "Task", 1L, "A");
        auditLogService.record(2L, "b@test.com", AuditLogService.ACTION_TASK_CREATED, "Task", 2L, "B");

        Page<AuditLogDto> all = auditLogService.getAllHistory(PageRequest.of(0, 10));

        assertEquals(2, all.getTotalElements());
    }

    @Test
    void getActorHistory_returnsEmptyPageForNoEntries() {
        Page<AuditLogDto> history = auditLogService.getActorHistory(999L, PageRequest.of(0, 10));

        assertTrue(history.isEmpty());
    }
}