package com.site.webapp.service;

import com.site.webapp.exception.mail.EmailSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class SmtpEmailService implements EmailService{

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;


    public SmtpEmailService(JavaMailSender mailSender,
                            @Value("${app.email.from:noreply@tasktracker.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            log.info("Отправка email на адрес: {}, тема: {}", to, subject);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email успешно отправлен на адрес: {}", to);
        } catch (MailException e) {
            log.error("Ошибка при отправке email на адрес {}: {}", to, e.getMessage());
            throw new EmailSendException("Не удалось отправить письмо на " + to + ". Попробуйте позже.", e);
        }
    }


    @Override
    public void sendInvite(String to, String inviteUrl) {
        String subject = "Приглашение в Task Tracker";
        String body = "Вы приглашены в Task Tracker. Перейдите по ссылке: " + inviteUrl;
        sendEmail(to, subject, body);
    }
}
