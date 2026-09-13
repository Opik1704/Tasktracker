package com.site.webapp.events;

import org.springframework.stereotype.Component;

/**
 *
 * @param taskId
 * @param taskTitle
 * @param artistId
 * @param deleterEmail
 */

public record TaskDeletedEvent(
        Long taskId,
        String taskTitle,
        Long artistId,
        String deleterEmail
) {}
