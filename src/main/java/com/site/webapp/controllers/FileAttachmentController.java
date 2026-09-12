package com.site.webapp.controllers;

import com.site.webapp.dto.ResourceDownloadDto;
import com.site.webapp.models.TaskAttachment;
import com.site.webapp.service.FileStorageService;
import com.site.webapp.service.TaskAttachmentService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class FileAttachmentController extends LoggingController{

    private final TaskAttachmentService taskAttachmentService;

    public FileAttachmentController(TaskAttachmentService taskAttachmentService) {
        this.taskAttachmentService = taskAttachmentService;
    }

    @GetMapping("/attachments/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long id) {
        ResourceDownloadDto dto = taskAttachmentService.downloadAttachment(id);

        String encodedFileName = URLEncoder.encode(dto.fileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType(dto.contentType()))
                .contentLength(dto.fileSize())
                .body(new InputStreamResource(dto.inputStream()));

    }
    @PostMapping("/attachments/{id}/delete")
    public String deleteAttachment(@PathVariable Long id, @RequestParam Long taskId) {

        taskAttachmentService.deleteAttachment(id);
        log.info("Вложение ID {} удалено из задачи ID {}", id, taskId);

        return "redirect:/all-tasks";

    }
}
