package com.site.webapp.dto;

import java.io.InputStream;

public record ResourceDownloadDto(
        InputStream inputStream,
        String fileName,
        String contentType,
        long fileSize
){}