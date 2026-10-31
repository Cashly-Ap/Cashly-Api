package com.cashly.cashly_api.platform.auth.mfa.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MfaAccount {
    public static final int MAX_FAILURES = 5;
    public static final int LOCKOUT_SECONDS = 900;
    private final String subjectId;
    private boolean enabled;
    private String secret;
    private Instant setupExpiresAt;
    private long lastStep;
    private final List<String> recoveryHashes;
    private String challengeHash;
    private Instant challengeExpiresAt;
    private int failures;
    private Instant lockedUntil;

    public MfaAccount(String subjectId, boolean enabled, String secret,
            Instant setupExpiresAt, long lastStep, List<String> recoveryHashes,
            String challengeHash, Instant challengeExpiresAt, int failures,
            Instant lockedUntil) {
        this.subjectId = subjectId;
        this.enabled = enabled;
        this.secret = secret;
        this.setupExpiresAt = setupExpiresAt;
        this.lastStep = lastStep;
        this.recoveryHashes = new ArrayList<>(recoveryHashes);
        this.challengeHash = challengeHash;
        this.challengeExpiresAt = challengeExpiresAt;
        this.failures = failures;
        this.lockedUntil = lockedUntil;
    }

    public String subjectId() {
        return subjectId;
    }
    public boolean enabled() {
        return enabled;
    }
    public String secret() {
        return secret;
    }
    public Instant setupExpiresAt() {
        return setupExpiresAt;
    }
    public long lastStep() {
        return lastStep;
    }
    public List<String> recoveryHashes() {
        return List.copyOf(recoveryHashes);
    }
    public String challengeHash() {
        return challengeHash;
    }
    public Instant challengeExpiresAt() {
        return challengeExpiresAt;
    }
    public int failures() {
        return failures;
    }
    public Instant lockedUntil() {
        return lockedUntil;
    }

    public void beginSetup(String encryptedSecret, Instant expiresAt) {
        secret = encryptedSecret;
        setupExpiresAt = expiresAt;
        lastStep = -1;
    }

    public void enable(List<String> hashes) {
        enabled = true;
        setupExpiresAt = null;
        clearChallenge();
        recoveryHashes.clear();
        recoveryHashes.addAll(hashes);
    }

    public void disable() {
        enabled = false;
        secret = null;
        recoveryHashes.clear();
        lastStep = -1;
        setupExpiresAt = null;
        clearChallenge();
    }

    public void challenge(String hash, Instant expiresAt) {
        challengeHash = hash;
        challengeExpiresAt = expiresAt;
    }

    public void clearChallenge() {
        challengeHash = null;
        challengeExpiresAt = null;
    }

    public void acceptStep(long step) {
        lastStep = step;
    }
    public boolean consumeRecoveryHash(String hash) {
        return recoveryHashes.remove(hash);
    }
    public boolean lockedAt(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }
    public void resetFailures() {
        failures = 0;
        lockedUntil = null;
    }
    public void registerFailure(Instant now) {
        failures++;
        if (failures >= MAX_FAILURES)
            lockedUntil = now.plusSeconds(LOCKOUT_SECONDS);
    }
}
