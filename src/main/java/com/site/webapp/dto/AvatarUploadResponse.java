package com.site.webapp.dto;

/**
 * Response DTO для загрузки аватара.
 * Содержит ключ в S3 и URL для доступа к загруженному файлу.
 */
public record AvatarUploadResponse(String s3Key,
                                   String url) {
}