package com.cashly.cashly_api.platform.auth.user.verification.application.services;

import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.platform.auth.user.verification.application.exceptions.EmailVerificationExpiredTokenException;
import com.cashly.cashly_api.platform.auth.user.verification.application.exceptions.EmailVerificationInvalidTokenException;
import com.cashly.cashly_api.platform.auth.user.verification.application.exceptions.EmailVerificationRateLimitedException;
import com.cashly.cashly_api.platform.auth.user.verification.application.exceptions.EmailVerificationUsedTokenException;
import com.cashly.cashly_api.platform.auth.user.verification.application.ports.out.EmailVerificationStore;
import com.cashly.cashly_api.platform.auth.user.verification.application.ports.out.VerificationMailSender;
import com.cashly.cashly_api.platform.auth.user.verification.domain.entity.VerificationToken;
import com.cashly.cashly_api.platform.auth.user.verification.domain.services.VerificationTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    private static final String BASE_URL = "http://localhost:4200/platform/auth/verify-email";
    private static final Duration TTL = Duration.ofHours(24);
    private static final Duration COOLDOWN = Duration.ofSeconds(60);
    private static final String LANGUAGE = "es";

    @Mock
    private EmailVerificationStore store;

    @Mock
    private UserRepository users;

    @Mock
    private UserPreferencesRepository preferences;

    @Mock
    private VerificationMailSender mailSender;

    private EmailVerificationService service;

    private User user;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(store, users, preferences,
                mailSender, BASE_URL, TTL, COOLDOWN);
        user = new User(UserId.generate(), new Email("user@example.com"),
                new UserName("Test User"),
                Password.fromHash("$2a$10$hashedPassword"));
    }

    @Test
    void requestVerificationStoresOnlyTheTokenHashAndSendsTheRawLink() {
        service.requestVerification(user, LANGUAGE);

        ArgumentCaptor<VerificationToken> stored = ArgumentCaptor
                .forClass(VerificationToken.class);
        verify(store).create(stored.capture());
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(mailSender).send(eq("user@example.com"), eq("Test User"),
                link.capture(), eq(LANGUAGE));

        String rawToken = link.getValue().substring(BASE_URL.length() + "?token=".length());
        assertThat(link.getValue()).startsWith(BASE_URL + "?token=");
        assertThat(stored.getValue().tokenHash())
                .isEqualTo(VerificationTokens.hash(rawToken));
        assertThat(stored.getValue().tokenHash()).isNotEqualTo(rawToken);
        assertThat(stored.getValue().expiresAt())
                .isAfter(Instant.now().plus(TTL.minusSeconds(5)));
    }

    @Test
    void requestVerificationInvalidatesPreviousTokensBeforeIssuingANewOne() {
        service.requestVerification(user, LANGUAGE);

        verify(store).invalidateActive(eq(user.getId().getValue()), any(Instant.class));
    }

    @Test
    void requestVerificationIsSkippedWhenTheEmailIsAlreadyVerified() {
        user.verifyEmail();

        service.requestVerification(user, LANGUAGE);

        verify(store, never()).create(any());
        verify(mailSender, never()).send(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void resendIsRateLimitedWithinTheCooldownWindow() {
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(store.lastIssuedAt(user.getId().getValue()))
                .thenReturn(Optional.of(Instant.now().minusSeconds(5)));

        assertThatThrownBy(() -> service.resend(user.getId().getValue()))
                .isInstanceOf(EmailVerificationRateLimitedException.class);
        verify(mailSender, never()).send(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void resendIssuesANewTokenAfterTheCooldown() {
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(store.lastIssuedAt(user.getId().getValue()))
                .thenReturn(Optional.of(Instant.now().minusSeconds(120)));
        when(preferences.languageFor(user.getId().getValue())).thenReturn(LANGUAGE);

        service.resend(user.getId().getValue());

        verify(mailSender).send(eq("user@example.com"), eq("Test User"),
                anyString(), eq(LANGUAGE));
    }

    @Test
    void resendIsANoOpWhenTheEmailIsAlreadyVerified() {
        user.verifyEmail();
        when(users.findById(user.getId())).thenReturn(Optional.of(user));

        service.resend(user.getId().getValue());

        verify(store, never()).create(any());
    }

    @Test
    void confirmVerifiesTheUserAndConsumesTheToken() {
        String rawToken = "raw-token";
        VerificationToken token = activeToken(rawToken);
        when(store.findByHash(VerificationTokens.hash(rawToken)))
                .thenReturn(Optional.of(token));
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(store.markUsed(eq(token.id()), any(Instant.class))).thenReturn(true);

        service.confirm(rawToken);

        assertThat(user.isEmailVerified()).isTrue();
        verify(users).save(user);
        verify(store).markUsed(eq(token.id()), any(Instant.class));
    }

    @Test
    void confirmRejectsAnUnknownToken() {
        when(store.findByHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirm("unknown"))
                .isInstanceOf(EmailVerificationInvalidTokenException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void confirmRejectsABlankToken() {
        assertThatThrownBy(() -> service.confirm("  "))
                .isInstanceOf(EmailVerificationInvalidTokenException.class);
        verify(store, never()).findByHash(anyString());
    }

    @Test
    void confirmRejectsAnExpiredToken() {
        String rawToken = "expired-token";
        VerificationToken expired = new VerificationToken(UUID.randomUUID(),
                user.getId().getValue(), VerificationTokens.hash(rawToken),
                Instant.now().minusSeconds(1), null);
        when(store.findByHash(VerificationTokens.hash(rawToken)))
                .thenReturn(Optional.of(expired));
        when(users.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.confirm(rawToken))
                .isInstanceOf(EmailVerificationExpiredTokenException.class)
                .hasMessageContaining("expired");
        assertThat(user.isEmailVerified()).isFalse();
        verify(users, never()).save(any());
    }

    @Test
    void confirmRejectsAUsedTokenWhenTheEmailIsStillUnverified() {
        String rawToken = "used-token";
        VerificationToken used = new VerificationToken(UUID.randomUUID(),
                user.getId().getValue(), VerificationTokens.hash(rawToken),
                Instant.now().plusSeconds(3600), Instant.now().minusSeconds(10));
        when(store.findByHash(VerificationTokens.hash(rawToken)))
                .thenReturn(Optional.of(used));
        when(users.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.confirm(rawToken))
                .isInstanceOf(EmailVerificationUsedTokenException.class)
                .hasMessageContaining("used");
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    void confirmIsIdempotentWhenTheEmailIsAlreadyVerified() {
        String rawToken = "used-token";
        user.verifyEmail();
        when(store.findByHash(VerificationTokens.hash(rawToken)))
                .thenReturn(Optional.of(new VerificationToken(UUID.randomUUID(),
                        user.getId().getValue(), VerificationTokens.hash(rawToken),
                        Instant.now().plusSeconds(3600), Instant.now())));
        when(users.findById(user.getId())).thenReturn(Optional.of(user));

        service.confirm(rawToken);

        verify(users, never()).save(any());
    }

    @Test
    void confirmFailsWhenTheTokenIsConsumedConcurrently() {
        String rawToken = "race-token";
        VerificationToken token = activeToken(rawToken);
        when(store.findByHash(VerificationTokens.hash(rawToken)))
                .thenReturn(Optional.of(token));
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(store.markUsed(eq(token.id()), any(Instant.class))).thenReturn(false);

        assertThatThrownBy(() -> service.confirm(rawToken))
                .isInstanceOf(EmailVerificationUsedTokenException.class)
                .hasMessageContaining("used");
        assertThat(user.isEmailVerified()).isFalse();
        verify(users, never()).save(any());
    }

    private VerificationToken activeToken(String rawToken) {
        return new VerificationToken(UUID.randomUUID(),
                user.getId().getValue(), VerificationTokens.hash(rawToken),
                Instant.now().plus(TTL), null);
    }
}
