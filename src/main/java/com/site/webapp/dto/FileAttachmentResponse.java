package com.site.webapp.dto;

/**
 * Response DTO для прикрепленного файла.
 * Содержит информацию о загруженном файле: ключ в S3, URL, оригинальное имя, размер и тип контента.
 */
public record FileAttachmentResponse(String s3Key,
                                     String url,
                                     String originalFileName,
                                     Long size,
                                     String contentType) {
}