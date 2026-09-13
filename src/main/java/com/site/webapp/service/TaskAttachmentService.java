package com.site.webapp.service;

import com.site.webapp.dto.ResourceDownloadDto;
import com.site.webapp.events.FileAttachedEvent;
import com.site.webapp.models.Task;
import com.site.webapp.models.TaskAttachment;
import com.site.webapp.repo.TaskAttachmentRepository;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.security.CustomUserDetails;
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

    public TaskAttachmentService(FileStorageService fileStorageService,
                                 TaskAttachmentRepository taskAttachmentRepository,
                                 TaskRepository taskRepository,
                                 ApplicationEventPublisher eventPublisher) {
        this.fileStorageService = fileStorageService;
        this.taskAttachmentRepository = taskAttachmentRepository;
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public String addAttachment(MultipartFile file,Long taskId){
        if(file == null || file.isEmpty()){
            throw new IllegalArgumentException("Файл не может быть пустым");
        }
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Невозможно найти задачу с ID: " + taskId));

        String s3Key = fileStorageService.uploadFile(file, "task-attachments");

        TaskAttachment taskAttachment = new TaskAttachment();
        taskAttachment.setTask(task);
        taskAttachment.setS3Key(s3Key);
        taskAttachment.setFileName(file.getOriginalFilename());
        taskAttachment.setContentType(file.getContentType());
        taskAttachment.setFileSize(file.getSize());

        taskAttachmentRepository.save(taskAttachment);

        Long currentUserId = getCurrentUserId();

        boolean isUploadedByArtist = currentUserId != null && currentUserId.equals(task.getArtistId());

        if(task.getArtistId()!= null && !isUploadedByArtist){
            eventPublisher.publishEvent(new FileAttachedEvent(
                    taskId,
                    task.getArtistId(),
                    file.getOriginalFilename()
            ));
        }
        log.info("Вложение успешно сохранено для задачи ID: {}, s3Key: {}", taskId, s3Key);
        return s3Key;
    }

    public List<TaskAttachment> getAttachmentsByTaskId(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("Невозможно найти задачу с ID: " + taskId);
        }
        return taskAttachmentRepository.findByTaskId(taskId);
    }

    public ResourceDownloadDto downloadAttachment(Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Вложение не найдено с ID: " + attachmentId));

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
        TaskAttachment taskAttachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Не найдено вложение с ID: " + attachmentId));

        fileStorageService.deleteFile(taskAttachment.getS3Key());

        taskAttachmentRepository.delete(taskAttachment);
        log.info("Вложение  '{}' успешно удалено из S3 и БД",taskAttachment.getFileName());
    }

    @Transactional
    public void deleteAllByTaskId(Long taskId) {
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
        return null;
    }
}
