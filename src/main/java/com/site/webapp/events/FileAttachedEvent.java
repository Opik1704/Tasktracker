package com.site.webapp.events;

public record FileAttachedEvent(
        Long taskId,
        Long artistId,
        String fileName
) {
}
