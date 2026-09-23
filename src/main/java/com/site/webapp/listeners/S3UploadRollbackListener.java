package com.site.webapp.listeners;

import com.site.webapp.events.AvatarUploadedEvent;
import com.site.webapp.events.FileAttachedEvent;
import com.site.webapp.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class S3UploadRollbackListener {
    private static final Logger log = LoggerFactory.getLogger(S3UploadRollbackListener.class);
    private final FileStorageService fileStorageService;

    public S3UploadRollbackListener(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleAvatarUploadRollback(AvatarUploadedEvent event) {
        if (event.s3Key() != null && !event.s3Key().isBlank()) {
            log.warn("Транзакция БД откачена! Удаляем загруженный аватар из S3: {}", event.s3Key());
            deleteQuietly(event.s3Key());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleFileAttachRollback(FileAttachedEvent event) {
        if (event.s3Key() != null && !event.s3Key().isBlank()) {
            log.warn("Транзакция БД откачена! Удаляем загруженное вложение из S3: {}", event.s3Key());
            deleteQuietly(event.s3Key());
        }
    }

    private void deleteQuietly(String s3Key) {
        try {
            fileStorageService.deleteFile(s3Key);
            log.info("Файл успешно очищен из S3 после отката: {}", s3Key);
        } catch (Exception e) {
            log.error("Не удалось удалить файл {} из S3 при откате транзакции БД!", s3Key, e);
        }
    }
}