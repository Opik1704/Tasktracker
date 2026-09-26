package com.site.webapp.events;

import com.site.webapp.models.Task;

import java.time.LocalDateTime;

/**
 *  * Событие создания новой задачи.
 * @param taskId
 * @param taskTitle
 * @param deadline
 * @param priority
 * @param artistId
 * @param creatorFullName
 */
public record TaskCreatedEvent(
        Long taskId,
        String taskTitle,
        LocalDateTime deadline,
        Task.Priority priority,
        Long artistId,
        String creatorFullName
) {}