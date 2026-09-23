package com.site.webapp.listeners;

import com.site.webapp.events.AvatarDeletedEvent;
import com.site.webapp.events.FileDeletedEvent;
import com.site.webapp.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class S3DeleteEventListener {
    private static final Logger log = LoggerFactory.getLogger(S3DeleteEventListener.class);

    private final FileStorageService fileStorageService;
    public S3DeleteEventListener(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAvatarDeleted(AvatarDeletedEvent event) {
        if (event.avatarS3Key() != null && !event.avatarS3Key().isBlank()) {
            log.info("Транзакция завершена. Удаляем аватар из S3 по ключу: {}", event.avatarS3Key());
            deleteFromS3(event.avatarS3Key());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFileDeleted(FileDeletedEvent event) {
        if (event.s3Key() != null && !event.s3Key().isBlank()) {
            log.info("Транзакция завершена. Удаляем вложение задачи из S3 по ключу: {}", event.s3Key());
            deleteFromS3(event.s3Key());
        }
    }

    private void deleteFromS3(String s3Key) {
        try {
            fileStorageService.deleteFile(s3Key);
            log.info("Файл успешно физически удален из S3: {}", s3Key);
        } catch (Exception e) {
            log.error("Не удалось удалить файл из S3 ({}) после коммита БД", s3Key, e);
        }
    }
}
