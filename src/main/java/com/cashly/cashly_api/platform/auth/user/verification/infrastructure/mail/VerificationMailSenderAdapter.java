package com.cashly.cashly_api.platform.auth.user.verification.infrastructure.mail;

import com.cashly.cashly_api.platform.auth.user.verification.application.ports.out.VerificationMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class VerificationMailSenderAdapter implements VerificationMailSender {

    private static final Logger log = LoggerFactory
            .getLogger(VerificationMailSenderAdapter.class);
    private static final String ENGLISH = "en";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String mailHost;
    private final String from;

    public VerificationMailSenderAdapter(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${cashly.email.from}") String from) {
        this.mailSenderProvider = mailSenderProvider;
        this.mailHost = mailHost;
        this.from = from;
    }

    @Override
    public void send(String recipientEmail, String recipientName,
            String verificationLink, String language) {
        if (mailHost == null || mailHost.isBlank()) {
            log.info(
                    "SMTP is not configured; verification link for {} is {}",
                    recipientEmail, verificationLink);
            return;
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn(
                    "No JavaMailSender available; verification link for {} is {}",
                    recipientEmail, verificationLink);
            return;
        }
        try {
            mailSender.send(message(recipientEmail, recipientName,
                    verificationLink, language));
        } catch (RuntimeException exception) {
            log.error("Could not send verification email to {}: {}",
                    recipientEmail, exception.getMessage());
        }
    }

    private SimpleMailMessage message(String recipientEmail,
            String recipientName, String verificationLink, String language) {
        boolean english = ENGLISH.equalsIgnoreCase(language);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipientEmail);
        message.setSubject(english ? "Verify your Cashly email address"
                : "Verifica tu correo de Cashly");
        message.setText(english ? englishBody(recipientName, verificationLink)
                : spanishBody(recipientName, verificationLink));
        return message;
    }

    private String spanishBody(String recipientName, String verificationLink) {
        return "Hola " + recipientName + ",\n\n"
                + "Confirma tu correo electrónico para completar la configuración de tu cuenta de Cashly.\n\n"
                + verificationLink + "\n\n"
                + "El enlace caduca en 24 horas y solo puede usarse una vez.\n"
                + "Si no solicitaste esta verificación, ignora este mensaje.\n";
    }

    private String englishBody(String recipientName, String verificationLink) {
        return "Hello " + recipientName + ",\n\n"
                + "Confirm your email address to finish setting up your Cashly account.\n\n"
                + verificationLink + "\n\n"
                + "The link expires in 24 hours and can be used only once.\n"
                + "If you did not request this verification, ignore this message.\n";
    }
}
