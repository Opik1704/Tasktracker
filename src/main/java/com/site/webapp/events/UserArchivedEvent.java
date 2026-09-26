package com.site.webapp.events;

/**
 * Событие архивации пользователя.

 * Публикуется после полной анонимизации данных пользователя.
 * Содержит ID пользователя и ключ аватара для удаления из S3.
 *
 * @param userId ID архивированного пользователя
 * @param avatarS3Key ключ аватара в S3 (null если аватара не было)
 */
public record UserArchivedEvent(
        Long userId,
        String avatarS3Key
) {
}
