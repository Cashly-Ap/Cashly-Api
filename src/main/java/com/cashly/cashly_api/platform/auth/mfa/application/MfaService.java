package com.cashly.cashly_api.platform.auth.mfa.application;

import com.cashly.cashly_api.platform.auth.authentication.application.dto.response.AuthenticatedEntityResponse;
import com.cashly.cashly_api.platform.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.platform.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.mfa.domain.MfaAccount;
import com.cashly.cashly_api.platform.auth.token.application.ports.out.AuthenticableSubjectGateway;
import com.cashly.cashly_api.platform.auth.token.domain.repository.RefreshTokenRepository;
import com.cashly.cashly_api.platform.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.platform.auth.user.domain.services.PasswordEncoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(noRollbackFor = InvalidCredentialsException.class)
public class MfaService {
    private static final int SETUP_SECONDS = 600;
    private static final int CHALLENGE_SECONDS = 300;
    private static final int RECOVERY_CODE_COUNT = 10;
    private static final int RECOVERY_CODE_LENGTH = 22;

    private final MfaStore store;
    private final MfaCryptography crypto;
    private final AuthenticableSubjectGateway subjects;
    private final PasswordEncoder passwords;
    private final RefreshTokenRepository refreshTokens;
    private final TokenService tokens;

    public MfaService(MfaStore store, MfaCryptography crypto, AuthenticableSubjectGateway subjects,
            PasswordEncoder passwords, RefreshTokenRepository refreshTokens, TokenService tokens) {
        this.store = store;
        this.crypto = crypto;
        this.subjects = subjects;
        this.passwords = passwords;
        this.refreshTokens = refreshTokens;
        this.tokens = tokens;
    }

    public record Status(boolean enabled, int recoveryCodesRemaining) {}
    public record Setup(String secret, String otpauthUri, int expiresIn) {}
    public record RecoveryCodes(List<String> recoveryCodes) {}

    public Status status(String subjectId) {
        MfaAccount account = store.lock(subjectId);
        return new Status(account.enabled(), account.recoveryHashes().size());
    }

    public Setup setup(String subjectId, String password) {
        MfaAccount account = store.lock(subjectId);
        checkLock(account);
        Authenticable subject = reauthenticate(account, password);
        if (account.enabled()) {
            throw new IllegalArgumentException("MFA is already enabled");
        }
        String secret = crypto.newSecret();
        account.beginSetup(crypto.encrypt(secret), Instant.now().plusSeconds(SETUP_SECONDS));
        store.save(account);
        String label = URLEncoder.encode("Cashly:" + subject.getEmail(), StandardCharsets.UTF_8)
            .replace("+", "%20");
        String uri = "otpauth://totp/" + label + "?secret=" + secret
            + "&issuer=Cashly&algorithm=SHA1&digits=6&period=30";
        return new Setup(secret, uri, SETUP_SECONDS);
    }

    public RecoveryCodes enable(String subjectId, String code) {
        MfaAccount account = store.lock(subjectId);
        checkLock(account);
        if (account.enabled() || account.secret() == null || account.setupExpiresAt() == null
                || !account.setupExpiresAt().isAfter(Instant.now())) {
            throw new InvalidCredentialsException("MFA setup has expired; start again");
        }
        if (!verifyCode(account, code, false)) {
            fail(account);
        }
        List<String> recoveryCodes = new ArrayList<>();
        List<String> recoveryHashes = new ArrayList<>();
        for (int index = 0; index < RECOVERY_CODE_COUNT; index++) {
            String recoveryCode = crypto.randomToken().substring(0, RECOVERY_CODE_LENGTH);
            recoveryCodes.add(recoveryCode);
            recoveryHashes.add(crypto.hash(recoveryCode));
        }
        account.enable(recoveryHashes);
        store.save(account);
        refreshTokens.revokeAllBySubjectId(subjectId);
        return new RecoveryCodes(List.copyOf(recoveryCodes));
    }

    public void disable(String subjectId, String password, String code) {
        MfaAccount account = store.lock(subjectId);
        checkLock(account);
        reauthenticate(account, password);
        if (!account.enabled() || !verifyCode(account, code, true)) {
            fail(account);
        }
        account.disable();
        store.save(account);
        refreshTokens.revokeAllBySubjectId(subjectId);
    }

    public AuthenticationResponse challengeIfEnabled(Authenticable subject) {
        MfaAccount account = store.lock(subject.getSubjectId());
        if (!account.enabled()) {
            return null;
        }
        checkLock(account);
        String challenge = crypto.randomToken();
        account.challenge(crypto.hash(challenge), Instant.now().plusSeconds(CHALLENGE_SECONDS));
        store.save(account);
        return AuthenticationResponse.mfaChallenge(challenge);
    }

    public AuthenticationResponse verify(String challengeToken, String code) {
        MfaAccount account = store.lockByChallenge(crypto.hash(challengeToken));
        if (account == null) {
            throw new InvalidCredentialsException("Invalid or expired MFA challenge");
        }
        checkLock(account);
        if (!account.enabled() || account.challengeExpiresAt() == null
                || !account.challengeExpiresAt().isAfter(Instant.now())) {
            throw new InvalidCredentialsException("Invalid or expired MFA challenge");
        }
        if (!verifyCode(account, code, true)) {
            fail(account);
        }
        Authenticable subject = subjects.resolve(account.subjectId(), AuthenticableType.USER);
        if (!subject.canAuthenticate()) {
            throw new InvalidCredentialsException("Account is not active");
        }
        account.clearChallenge();
        store.save(account);
        String accessToken = tokens.generateAccessToken(subject);
        var refreshToken = tokens.generateRefreshToken(subject);
        refreshTokens.save(refreshToken);
        return AuthenticationResponse.of(accessToken, refreshToken.getToken(),
            tokens.getAccessTokenExpiration(), AuthenticatedEntityResponse.from(subject));
    }

    private Authenticable reauthenticate(MfaAccount account, String password) {
        Authenticable subject = subjects.resolve(account.subjectId(), AuthenticableType.USER);
        if (!subject.canAuthenticate() || !passwords.matches(password, subject.getHashedPassword())) {
            fail(account);
        }
        return subject;
    }

    private boolean verifyCode(MfaAccount account, String code, boolean allowRecovery) {
        long step = crypto.verifyStep(crypto.decrypt(account.secret()), code, Instant.now(), account.lastStep());
        boolean valid = step >= 0;
        if (valid) {
            account.acceptStep(step);
        } else if (allowRecovery) {
            valid = account.consumeRecoveryHash(crypto.hash(code));
        }
        if (valid) {
            account.resetFailures();
        }
        return valid;
    }

    private void checkLock(MfaAccount account) {
        if (account.lockedAt(Instant.now())) {
            throw new InvalidCredentialsException("Too many MFA attempts; try again in 15 minutes");
        }
        if (account.lockedUntil() != null) {
            account.resetFailures();
        }
    }

    private void fail(MfaAccount account) {
        account.registerFailure(Instant.now());
        store.save(account);
        throw new InvalidCredentialsException("Invalid MFA code or current password");
    }
}
