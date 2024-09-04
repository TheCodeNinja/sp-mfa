package com.codeninja.mfa.service.impl;

import com.codeninja.mfa.model.entity.EmailConfirmationToken;
import com.codeninja.mfa.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender sender;

    public EmailServiceImpl(JavaMailSender sender) {
        this.sender = sender;
    }

    @Async
    @Override
    public void sendConfirmationEmail(EmailConfirmationToken emailConfirmationToken) throws MessagingException {
        log.info("** sendConfirmationEmail - payload: {}", emailConfirmationToken);
        //MIME - HTML message
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        //helper.setTo(emailConfirmationToken.getUser().getUsername());
        helper.setTo("marcus.malcomm@gmail.com");
        helper.setSubject("Confirm you E-Mail - MFA Application Registration");
        helper.setText("<html>" +
                        "<body>" +
                        "<h2>Dear "+ emailConfirmationToken.getUser().getFirstName() + ",</h2>"
                        + "<br/> We're excited to have you get started. " +
                        "Please click on below link to confirm your account."
                        + "<br/> "  + generateConfirmationLink(emailConfirmationToken.getToken())+"" +
                        "<br/> Regards,<br/>" +
                        "MFA Registration team" +
                        "</body>" +
                        "</html>"
                , true);

        sender.send(message);
    }

    private String generateConfirmationLink(String token){
        return "<a href=http://localhost:8080/confirm-email?token="+token+">Confirm Email</a>";
    }
}
