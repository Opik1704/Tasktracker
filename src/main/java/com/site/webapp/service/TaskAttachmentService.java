package com.site.webapp.service;

import com.site.webapp.config.AttachmentProperties;
import com.site.webapp.dto.ResourceDownloadDto;
import com.site.webapp.events.FileAttachedEvent;
import com.site.webapp.events.FileDeletedEvent;
import com.site.webapp.exception.TaskNotFoundException;
import com.site.webapp.models.Task;
import com.site.webapp.models.TaskAttachment;
import com.site.webapp.repo.TaskAttachmentRepository;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.security.CustomUserDetails;
import com.site.webapp.validator.FileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskAttachmentService {
    private static final Logger log = LoggerFactory.getLogger(TaskAttachmentService.class);

    private final FileStorageService fileStorageService;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FileValidator fileValidator;
    private final AttachmentProperties attachmentProperties;

    public TaskAttachmentService(FileStorageService fileStorageService,
                                 TaskAttachmentRepository taskAttachmentRepository,
                                 TaskRepository taskRepository,
                                 ApplicationEventPublisher eventPublisher,
                                 AttachmentProperties attachmentProperties,
                                 FileValidator fileValidator) {
        this.fileStorageService = fileStorageService;
        this.taskAttachmentRepository = taskAttachmentRepository;
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
        this.attachmentProperties = attachmentProperties;
        this.fileValidator = fileValidator;

    }

    @Transactional
    public String uploadAttachment(MultipartFile file,Long taskId){
        log.debug("Старт загрузки вложения для задачи ID: {}, файл: '{}', размер: {} байт", taskId, file.getOriginalFilename(), file.getSize());

        fileValidator.validateNotEmpty(file);
        fileValidator.validateSize(file, attachmentProperties.getMaxSizeBytes());
        String cleanExtension = fileValidator.sanitizeExtension(file, attachmentProperties.getAllowedTypes().keySet());
        String expectedMimeType = attachmentProperties.getAllowedTypes().get(cleanExtension);
        fileValidator.validateMagicBytes(file, expectedMimeType);
        log.trace("Файл '{}' прошел валидацию типа и Magic Bytes", file.getOriginalFilename());

        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Невозможно найти задачу с ID: " + taskId));

        String s3Key = fileStorageService.uploadFile(file, "task-attachments", cleanExtension);
        log.info("Вложение загружено в S3 с ключом: {}", s3Key);

        TaskAttachment taskAttachment = new TaskAttachment();
        taskAttachment.setTask(task);
        taskAttachment.setS3Key(s3Key);
        taskAttachment.setFileName(file.getOriginalFilename());
        taskAttachment.setContentType(file.getContentType());
        taskAttachment.setFileSize(file.getSize());

        taskAttachmentRepository.save(taskAttachment);

        Long currentUserId = getCurrentUserId();
        boolean isUploadedByArtist = currentUserId != null && currentUserId.equals(task.getArtistId());

        eventPublisher.publishEvent(new FileAttachedEvent(
                taskId,
                task.getArtistId(),
                s3Key
        ));
        log.info("Вложение успешно сохранено для задачи ID: {}, s3Key: {}", taskId, s3Key);
        return s3Key;
    }

    public List<TaskAttachment> getAttachmentsByTaskId(Long taskId) {
        log.debug("Запрос списка вложений для задачи ID: {}", taskId);
        if (!taskRepository.existsById(taskId)) {
            log.warn("Запрос вложений отклонен: задача ID {} не найдена", taskId);
            throw new IllegalArgumentException("Невозможно найти задачу с ID: " + taskId);
        }
        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);
        log.debug("Найдено вложений: {} для задачи ID: {}", attachments.size(), taskId);
        return attachments;
    }

    public ResourceDownloadDto downloadAttachment(Long attachmentId) {
        log.debug("Запрос на скачивание вложения ID: {}", attachmentId);
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> {
                    log.warn("Скачивание отклонено: вложение с ID {} не найдено в БД", attachmentId);
                    return new IllegalArgumentException("Вложение не найдено с ID: " + attachmentId);
                });

        log.info("Скачивание файла вложения '{}' (S3 key: {})", attachment.getFileName(), attachment.getS3Key());
        InputStream inputStream = fileStorageService.downloadFile(attachment.getS3Key());
        return new ResourceDownloadDto(
                inputStream,
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getFileSize()
        );
    }

    @Transactional
    public void deleteAttachment(Long attachmentId){
        log.debug("Запрос на удаление вложения ID: {}", attachmentId);
        TaskAttachment taskAttachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> {
                    log.warn("Удаление отклонено: вложение с ID {} не найдено", attachmentId);
                    return new IllegalArgumentException("Не найдено вложение с ID: " + attachmentId);
                });

        Long taskId = taskAttachment.getTask().getId();
        Long artistId = taskAttachment.getTask().getArtistId();
        String s3Key = taskAttachment.getS3Key();
        String fileName = taskAttachment.getFileName();

        taskAttachmentRepository.delete(taskAttachment);
        log.info("Запись вложения ID: {} удалена из БД", attachmentId);

        eventPublisher.publishEvent(new FileDeletedEvent(
                taskId,
                artistId,
                s3Key
        ));

        log.debug("Событие FileDeletedEvent отправлено для вложения ID: {}. Физическое удаление из S3 произойдет после коммита транзакции.", attachmentId);
    }

    @Transactional
    public void deleteAllByTaskId(Long taskId) {
        log.info("Запуск полного удаления всех вложений для задачи ID: {}", taskId);
        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);
        for (TaskAttachment attachment : attachments) {
            deleteAttachment(attachment.getId());
        }
        log.info("Все вложения для задачи ID {} успешно удалены", taskId);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        log.trace("Не удалось извлечь userId из текущего SecurityContextHolder");
        return null;
    }
}
