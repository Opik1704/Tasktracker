package com.site.webapp.events;

public record FileDeletedEvent(
        Long taskId,
        Long artistId,
        String fileName
) {
}
