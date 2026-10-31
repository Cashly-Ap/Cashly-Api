package com.cashly.cashly_api.platform.auth.mfa.application;

import com.cashly.cashly_api.platform.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.mfa.domain.MfaAccount;
import com.cashly.cashly_api.platform.auth.token.application.ports.out.AuthenticableSubjectGateway;
import com.cashly.cashly_api.platform.auth.token.domain.repository.RefreshTokenRepository;
import com.cashly.cashly_api.platform.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.platform.auth.user.domain.services.PasswordEncoder;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {
    @Mock
    MfaStore store;
    @Mock
    MfaCryptography crypto;
    @Mock
    AuthenticableSubjectGateway subjects;
    @Mock
    PasswordEncoder passwords;
    @Mock
    RefreshTokenRepository refresh;
    @Mock
    TokenService tokens;
    @Mock
    Authenticable subject;
    MfaService service;
    MfaAccount account;

    @BeforeEach
    void setup() {
        service = new MfaService(store, crypto, subjects, passwords, refresh,
                tokens);
        account = new MfaAccount("subject", true, "encrypted", null, -1,
                List.of("recovery-hash"), "challenge-hash",
                Instant.now().plusSeconds(300), 0, null);
    }

    @Test
    void password_login_returns_challenge_without_any_session_tokens() {
        when(subject.getSubjectId()).thenReturn("subject");
        when(store.lock("subject")).thenReturn(account);
        when(crypto.randomToken()).thenReturn("challenge");
        when(crypto.hash("challenge")).thenReturn("hash");
        var result = service.challengeIfEnabled(subject);
        assertTrue(result.mfaRequired());
        assertNull(result.accessToken());
        assertNull(result.refreshToken());
        assertEquals("challenge", result.challengeToken());
        verifyNoInteractions(tokens, refresh);
    }

    @Test
    void replacing_challenge_never_resets_account_attempt_budget() {
        account.registerFailure(Instant.now());
        when(subject.getSubjectId()).thenReturn("subject");
        when(store.lock("subject")).thenReturn(account);
        when(crypto.randomToken()).thenReturn("challenge");
        when(crypto.hash("challenge")).thenReturn("hash");
        service.challengeIfEnabled(subject);
        assertEquals(1, account.failures());
    }

    @Test
    void bad_codes_lock_account_and_reject_new_password_challenges() {
        when(crypto.hash("challenge")).thenReturn("challenge-hash");
        when(store.lockByChallenge("challenge-hash")).thenReturn(account);
        when(crypto.decrypt("encrypted")).thenReturn("secret");
        when(crypto.verifyStep(eq("secret"), eq("000000"), any(), eq(-1L)))
                .thenReturn(-1L);
        for (int attempt = 0; attempt < 5; attempt++) {
            assertThrows(InvalidCredentialsException.class,
                    () -> service.verify("challenge", "000000"));
        }
        assertTrue(account.lockedAt(Instant.now()));
        when(subject.getSubjectId()).thenReturn("subject");
        when(store.lock("subject")).thenReturn(account);
        assertThrows(InvalidCredentialsException.class,
                () -> service.challengeIfEnabled(subject));
        verify(store, times(5)).save(account);
        verifyNoInteractions(tokens, refresh);
    }

    @Test
    void expired_challenges_never_consume_codes_or_issue_tokens() {
        account.challenge("challenge-hash", Instant.now().minusSeconds(1));
        when(crypto.hash("challenge")).thenReturn("challenge-hash");
        when(store.lockByChallenge("challenge-hash")).thenReturn(account);
        assertThrows(InvalidCredentialsException.class,
                () -> service.verify("challenge", "123456"));
        assertEquals(1, account.recoveryHashes().size());
        verifyNoInteractions(tokens, refresh);
        verify(crypto, never()).decrypt(anyString());
    }

    @Test
    void disable_requires_password_and_consumes_recovery_code_and_revokes_sessions() {
        when(store.lock("subject")).thenReturn(account);
        when(subjects.resolve("subject", AuthenticableType.USER))
                .thenReturn(subject);
        when(subject.canAuthenticate()).thenReturn(true);
        when(subject.getHashedPassword()).thenReturn("password-hash");
        when(passwords.matches("password", "password-hash")).thenReturn(true);
        when(crypto.decrypt("encrypted")).thenReturn("secret");
        when(crypto.verifyStep(eq("secret"), eq("recovery"), any(), eq(-1L)))
                .thenReturn(-1L);
        when(crypto.hash("recovery")).thenReturn("recovery-hash");
        service.disable("subject", "password", "recovery");
        assertFalse(account.enabled());
        assertTrue(account.recoveryHashes().isEmpty());
        assertNull(account.secret());
        assertNull(account.challengeHash());
        verify(refresh).revokeAllBySubjectId("subject");
    }

    @Test
    void valid_second_factor_does_not_allow_an_inactive_account_to_sign_in() {
        when(crypto.hash("challenge")).thenReturn("challenge-hash");
        when(store.lockByChallenge("challenge-hash")).thenReturn(account);
        when(crypto.decrypt("encrypted")).thenReturn("secret");
        when(crypto.verifyStep(eq("secret"), eq("123456"), any(), eq(-1L)))
                .thenReturn(42L);
        when(subjects.resolve("subject", AuthenticableType.USER))
                .thenReturn(subject);
        when(subject.canAuthenticate()).thenReturn(false);
        assertThrows(InvalidCredentialsException.class,
                () -> service.verify("challenge", "123456"));
        verifyNoInteractions(tokens, refresh);
        verify(store, never()).save(any());
    }

    @Test
    void expired_enrollment_cannot_be_enabled() {
        account = new MfaAccount("subject", false, "encrypted",
                Instant.now().minusSeconds(1), -1, List.of(), null, null, 0,
                null);
        when(store.lock("subject")).thenReturn(account);
        assertThrows(InvalidCredentialsException.class,
                () -> service.enable("subject", "123456"));
        assertFalse(account.enabled());
        verifyNoInteractions(crypto, tokens, refresh);
    }
}
