package com.site.webapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MockEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(MockEmailService.class);

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("[ EMAIL] Заглушка отправки письма.");
        log.info("   Кому: {}", to);
        log.info("   Тема: {}", subject);
        log.info("   Текст: {}", body);
    }

    @Override
    public void sendInvite(String to, String inviteUrl) {
        log.info("[ EMAIL] Заглушка отправки инвайта.");
        log.info("   Кому: {}", to);
        log.info("   Ссылка: {}", inviteUrl);
    }
}
