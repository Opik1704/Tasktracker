package com.site.webapp.events;

public record AvatarUploadedEvent(
    Long userId,
    String fileName,
    String s3Key
) {}
