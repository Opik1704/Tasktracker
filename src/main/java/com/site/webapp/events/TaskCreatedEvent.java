package com.site.webapp.events;

/**
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
        String deadline,
        String priority,
        Long artistId,
        String creatorFullName
) {}