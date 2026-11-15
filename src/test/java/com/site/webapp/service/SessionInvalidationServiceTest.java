package com.site.webapp.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * Юнит-тесты отзыва сессий. Используется мок JdbcIndexedSessionRepository,
 * так как в тестовом контексте реальный bean может отсутствовать.
 */
class SessionInvalidationServiceTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void invalidateSessions_deletesAllSessionsForPrincipal() {
        JdbcIndexedSessionRepository repo = mock(JdbcIndexedSessionRepository.class);
        Session session = mock(Session.class);
        Map sessions = new HashMap();
        sessions.put("s1", session);
        sessions.put("s2", session);

        doReturn(sessions).when(repo).findByIndexNameAndIndexValue(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, "user@test.com");

        ObjectProvider<JdbcIndexedSessionRepository> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(repo);

        SessionInvalidationService service = new SessionInvalidationService(provider);

        service.invalidateSessionsByPrincipal("user@test.com");

        verify(repo, times(2)).deleteById(anyString());
    }

    @Test
    @SuppressWarnings("rawtypes")
    void invalidateSessions_noRepositoryPresent_isNoOp() {
        ObjectProvider<JdbcIndexedSessionRepository> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);

        SessionInvalidationService service = new SessionInvalidationService(provider);

        assertDoesNotThrow(() -> service.invalidateSessionsByPrincipal("user@test.com"));
    }

    @Test
    void invalidateSessions_blankPrincipal_isNoOp() {
        JdbcIndexedSessionRepository repo = mock(JdbcIndexedSessionRepository.class);
        ObjectProvider<JdbcIndexedSessionRepository> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(repo);

        SessionInvalidationService service = new SessionInvalidationService(provider);

        assertDoesNotThrow(() -> service.invalidateSessionsByPrincipal(""));

        verifyNoInteractions(repo);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void invalidateSessions_noSessions_isNoOp() {
        JdbcIndexedSessionRepository repo = mock(JdbcIndexedSessionRepository.class);
        doReturn(new HashMap()).when(repo).findByIndexNameAndIndexValue(anyString(), anyString());

        ObjectProvider<JdbcIndexedSessionRepository> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(repo);

        SessionInvalidationService service = new SessionInvalidationService(provider);

        assertDoesNotThrow(() -> service.invalidateSessionsByPrincipal("user@test.com"));

        verify(repo, never()).deleteById(anyString());
    }
}