package com.site.webapp.events;

public record UserArchivedEvent(
        Long userId,
        String avatarS3Key
) {
}
