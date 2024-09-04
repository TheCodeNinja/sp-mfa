package com.codeninja.mfa.service;

import com.codeninja.mfa.model.entity.EmailConfirmationToken;
import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Async;

public interface EmailService {

    @Async
    void sendConfirmationEmail(EmailConfirmationToken emailConfirmationToken) throws MessagingException;
}