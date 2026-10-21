package com.cashly.cashly_api.platform.auth.mfa.infrastructure;

import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TotpCryptographyTest {
    private final TotpCryptography crypto = new TotpCryptography(Base64.getEncoder().encodeToString(new byte[32]));
    private static final String RFC_SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    @Test
    void matches_rfc6238_sha1_vectors_with_six_digits() {
        assertEquals("287082", crypto.totp(RFC_SECRET, 59 / 30));
        assertEquals("081804", crypto.totp(RFC_SECRET, 1111111109L / 30));
        assertEquals("050471", crypto.totp(RFC_SECRET, 1111111111L / 30));
        assertEquals("279037", crypto.totp(RFC_SECRET, 2000000000L / 30));
    }

    @Test
    void permits_one_step_clock_skew_but_rejects_replay_and_invalid_format() {
        Instant now = Instant.ofEpochSecond(120);
        assertEquals(3, crypto.verifyStep(RFC_SECRET, crypto.totp(RFC_SECRET, 3), now, -1));
        assertEquals(5, crypto.verifyStep(RFC_SECRET, crypto.totp(RFC_SECRET, 5), now, -1));
        assertEquals(-1, crypto.verifyStep(RFC_SECRET, crypto.totp(RFC_SECRET, 4), now, 4));
        assertEquals(-1, crypto.verifyStep(RFC_SECRET, crypto.totp(RFC_SECRET, 2), now, -1));
        assertEquals(-1, crypto.verifyStep(RFC_SECRET, "12345x", now, -1));
    }

    @Test
    void encrypts_with_random_nonce_and_authenticated_ciphertext() {
        String encrypted = crypto.encrypt(RFC_SECRET);
        assertEquals(RFC_SECRET, crypto.decrypt(encrypted));
        assertNotEquals(encrypted, crypto.encrypt(RFC_SECRET));
        assertFalse(encrypted.contains(RFC_SECRET));
        byte[] modified = Base64.getDecoder().decode(encrypted);
        modified[modified.length - 1] ^= 1;
        assertThrows(IllegalStateException.class, () -> crypto.decrypt(Base64.getEncoder().encodeToString(modified)));
        assertThrows(IllegalStateException.class, () -> new TotpCryptography("").encrypt(RFC_SECRET));
    }

    @Test
    void creates_authenticator_compatible_secrets_and_unpredictable_tokens() {
        assertTrue(crypto.newSecret().matches("[A-Z2-7]{32}"));
        assertNotEquals(crypto.newSecret(), crypto.newSecret());
        assertNotEquals(crypto.randomToken(), crypto.randomToken());
    }
}
