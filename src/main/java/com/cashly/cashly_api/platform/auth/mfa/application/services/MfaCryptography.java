package com.cashly.cashly_api.platform.auth.mfa.application.services;

import java.time.Instant;
public interface MfaCryptography {
    String newSecret();
    String randomToken();
    String encrypt(String secret);
    String decrypt(String ciphertext);
    String hash(String value);
    long verifyStep(String secret, String code, Instant now, long lastStep);
}
