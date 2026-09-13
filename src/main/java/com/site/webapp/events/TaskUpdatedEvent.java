package com.site.webapp.events;

import com.site.webapp.models.Task;

import java.time.LocalDateTime;

/**
 *
 * @param taskId
 * @param initiatorName
 * @param oldArtistId
 * @param newArtistId
 * @param oldDeadline
 * @param newDeadline
 * @param oldPriority
 * @param newPriority
 */
public record TaskUpdatedEvent(
        Long taskId,
        String taskTitle,
        String initiatorName,
        Long oldArtistId,
        Long newArtistId,
        LocalDateTime oldDeadline,
        LocalDateTime newDeadline,
        Task.Priority oldPriority,
        Task.Priority newPriority
) {}
