package com.site.webapp.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendInvite(String to, String inviteUrl);

}
