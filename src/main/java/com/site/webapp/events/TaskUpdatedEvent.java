package com.site.webapp.events;

import com.site.webapp.models.Task;

import java.time.LocalDateTime;

/**
 * Событие обновления задачи.
 * Содержит старые и новые значения для определения изменений.
 * Используется для отправки уведомлений при смене исполнителя, дедлайна или приоритета.
 * @param taskId ID задачи
 * @param taskTitle название задачи
 * @param initiatorName имя пользователя, изменившего задачу
 * @param oldArtistId предыдущий исполнитель
 * @param newArtistId новый исполнитель
 * @param oldDeadline предыдущий дедлайн
 * @param newDeadline новый дедлайн
 * @param oldPriority предыдущий приоритет
 * @param newPriority новый приоритет
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
