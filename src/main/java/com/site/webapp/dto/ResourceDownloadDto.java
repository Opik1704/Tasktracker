package com.site.webapp.dto;

import java.io.InputStream;

/**
 * Response DTO для скачивания файла.
 * Содержит поток данных файла, оригинальное имя, тип контента и размер.
 * Используется для возврата файлов пользователю (аватары, вложения задач).
 */

public record ResourceDownloadDto(
        InputStream inputStream,
        String fileName,
        String contentType,
        long fileSize
){}