package com.cashly.cashly_api.platform.auth.user.verification.application.services;

import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.platform.auth.user.verification.application.exceptions.EmailVerificationExpiredTokenException;
import com.cashly.cashly_api.platform.auth.user.verification.application.exceptions.EmailVerificationInvalidTokenException;
import com.cashly.cashly_api.platform.auth.user.verification.application.exceptions.EmailVerificationRateLimitedException;
import com.cashly.cashly_api.platform.auth.user.verification.application.exceptions.EmailVerificationUsedTokenException;
import com.cashly.cashly_api.platform.auth.user.verification.application.ports.out.EmailVerificationStore;
import com.cashly.cashly_api.platform.auth.user.verification.application.ports.out.VerificationMailSender;
import com.cashly.cashly_api.platform.auth.user.verification.domain.entity.VerificationToken;
import com.cashly.cashly_api.platform.auth.user.verification.domain.services.VerificationTokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private static final String DEFAULT_LANGUAGE = "es";
    private static final String TOKEN_QUERY_PARAMETER = "?token=";
    private static final String INVALID_TOKEN_MESSAGE = "Verification token is invalid";

    private final EmailVerificationStore store;
    private final UserRepository users;
    private final UserPreferencesRepository preferences;
    private final VerificationMailSender mailSender;
    private final String baseUrl;
    private final Duration ttl;
    private final Duration cooldown;

    public EmailVerificationService(EmailVerificationStore store,
            UserRepository users, UserPreferencesRepository preferences,
            VerificationMailSender mailSender,
            @Value("${cashly.email.verification.base-url}") String baseUrl,
            @Value("${cashly.email.verification.ttl}") Duration ttl,
            @Value("${cashly.email.verification.cooldown}") Duration cooldown) {
        this.store = store;
        this.users = users;
        this.preferences = preferences;
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
        this.ttl = ttl;
        this.cooldown = cooldown;
    }

    @Transactional
    public void requestVerification(User user, String language) {
        if (user.isEmailVerified()) {
            return;
        }
        issue(user, normalizeLanguage(language));
    }

    @Transactional
    public void resend(UUID userId) {
        User user = users.findById(new UserId(userId))
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId));
        if (user.isEmailVerified()) {
            return;
        }
        Instant now = Instant.now();
        store.lastIssuedAt(userId).ifPresent(lastIssued -> {
            if (lastIssued.plus(cooldown).isAfter(now)) {
                throw new EmailVerificationRateLimitedException(
                        "Wait before requesting a new verification email");
            }
        });
        issue(user, languageFor(userId));
    }

    @Transactional
    public void confirm(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new EmailVerificationInvalidTokenException(
                    INVALID_TOKEN_MESSAGE);
        }
        Instant now = Instant.now();
        VerificationToken token = store
                .findByHash(VerificationTokens.hash(rawToken.trim()))
                .orElseThrow(() -> new EmailVerificationInvalidTokenException(
                        INVALID_TOKEN_MESSAGE));
        User user = users.findById(new UserId(token.userId()))
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + token.userId()));
        if (user.isEmailVerified()) {
            return;
        }
        if (token.isUsed()) {
            throw new EmailVerificationUsedTokenException(
                    "Verification token has already been used");
        }
        if (token.isExpired(now)) {
            throw new EmailVerificationExpiredTokenException(
                    "Verification token has expired");
        }
        if (!store.markUsed(token.id(), now)) {
            throw new EmailVerificationUsedTokenException(
                    "Verification token has already been used");
        }
        user.verifyEmail();
        users.save(user);
    }

    private void issue(User user, String language) {
        Instant now = Instant.now();
        String rawToken = VerificationTokens.generate();
        store.invalidateActive(user.getId().getValue(), now);
        store.create(new VerificationToken(UUID.randomUUID(),
                user.getId().getValue(), VerificationTokens.hash(rawToken),
                now.plus(ttl), null));
        mailSender.send(user.getEmail().getValue(),
                user.getUserName().getValue(),
                baseUrl + TOKEN_QUERY_PARAMETER + rawToken, language);
    }

    private String languageFor(UUID userId) {
        return normalizeLanguage(preferences.languageFor(userId));
    }

    private String normalizeLanguage(String language) {
        return language == null || language.isBlank()
                ? DEFAULT_LANGUAGE
                : language;
    }
}
