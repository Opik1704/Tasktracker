package com.site.webapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Отзывает активные HTTP-сессии пользователя при soft delete и архивировании.
 * <p>
 * Сессии хранятся в JDBC Spring Session (таблица SPRING_SESSION) и индексируются
 * по имени principal. Для form-login principal name — email пользователя
 * ({@code CustomUserDetails.getUsername()}).
 * <p>
 * Зависимость на {@link JdbcIndexedSessionRepository} не обязательна: в контекстах
 * без настроенного Spring Session (например, в части тестов) сервис безопасно
 * деградирует до no-op, не ломая запуск приложения.
 */
@Service
public class SessionInvalidationService {

    private static final Logger log = LoggerFactory.getLogger(SessionInvalidationService.class);

    private final ObjectProvider<JdbcIndexedSessionRepository> sessionRepositoryProvider;

    public SessionInvalidationService(ObjectProvider<JdbcIndexedSessionRepository> sessionRepositoryProvider) {
        this.sessionRepositoryProvider = sessionRepositoryProvider;
    }

    /**
     * Удаляет все сессии пользователя по его email (principal name для form-login).
     *
     * @param userEmail email пользователя, он же principal name
     */
    @Transactional
    public void invalidateSessionsByPrincipal(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            log.warn("Попытка отозвать сессии с пустым principal name");
            return;
        }

        JdbcIndexedSessionRepository sessionRepository = sessionRepositoryProvider.getIfAvailable();
        if (sessionRepository == null) {
            log.warn("Spring Session JDBC не настроен — отзыв сессий для {} пропущен", userEmail);
            return;
        }

        Map<String, ? extends Session> sessions = sessionRepository
                .findByIndexNameAndIndexValue(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, userEmail);

        if (sessions.isEmpty()) {
            log.debug("Активных сессий для principal {} не найдено", userEmail);
            return;
        }

        sessions.keySet().forEach(sessionRepository::deleteById);
        log.info("Отозвано {} активных сессий пользователя {}", sessions.size(), userEmail);
    }
}