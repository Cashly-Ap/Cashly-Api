package com.cashly.cashly_api.platform.auth.mfa.infrastructure;

import com.cashly.cashly_api.platform.auth.mfa.application.MfaCryptography;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TotpCryptography implements MfaCryptography {
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_BYTES = 20;
    private static final int TOKEN_BYTES = 32;
    private static final int AES_KEY_BYTES = 32;
    private static final int NONCE_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int STEP_SECONDS = 30;
    private static final int CODE_MODULUS = 1_000_000;
    private static final String CIPHER = "AES/GCM/NoPadding";
    private final SecureRandom random = new SecureRandom();
    private final String configuredKey;

    public TotpCryptography(@Value("${app.mfa.encryption-key:}") String key) {
        this.configuredKey = key;
    }

    private byte[] randomBytes(int count) {
        byte[] bytes = new byte[count];
        random.nextBytes(bytes);
        return bytes;
    }

    public String randomToken() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(TOKEN_BYTES));
    }

    public String newSecret() {
        StringBuilder encoded = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte value : randomBytes(SECRET_BYTES)) {
            buffer = (buffer << 8) | (value & 255);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                encoded.append(BASE32.charAt((buffer >> bits) & 31));
            }
        }
        return encoded.toString();
    }

    private SecretKeySpec encryptionKey() {
        try {
            byte[] key = Base64.getDecoder().decode(configuredKey);
            if (key.length != AES_KEY_BYTES) {
                throw new IllegalArgumentException();
            }
            return new SecretKeySpec(key, "AES");
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Configure MFA_ENCRYPTION_KEY with a Base64 encoded 32 byte key before enabling MFA");
        }
    }

    public String encrypt(String secret) {
        try {
            byte[] nonce = randomBytes(NONCE_BYTES);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey(), new GCMParameterSpec(GCM_TAG_BITS, nonce));
            byte[] ciphertext = cipher.doFinal(secret.getBytes(StandardCharsets.US_ASCII));
            byte[] encrypted = ByteBuffer.allocate(nonce.length + ciphertext.length)
                .put(nonce).put(ciphertext).array();
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("MFA encryption failed", exception);
        }
    }

    public String decrypt(String ciphertext) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(ciphertext);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey(),
                new GCMParameterSpec(GCM_TAG_BITS, encrypted, 0, NONCE_BYTES));
            byte[] secret = cipher.doFinal(encrypted, NONCE_BYTES, encrypted.length - NONCE_BYTES);
            return new String(secret, StandardCharsets.US_ASCII);
        } catch (Exception exception) {
            throw new IllegalStateException("MFA decryption failed", exception);
        }
    }

    public String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    public long verifyStep(String secret, String code, Instant now, long lastStep) {
        if (code == null || !code.matches("[0-9]{6}")) {
            return -1;
        }
        long currentStep = now.getEpochSecond() / STEP_SECONDS;
        byte[] submitted = code.getBytes(StandardCharsets.US_ASCII);
        for (long step = currentStep - 1; step <= currentStep + 1; step++) {
            if (step > lastStep && MessageDigest.isEqual(totp(secret, step).getBytes(StandardCharsets.US_ASCII), submitted)) {
                return step;
            }
        }
        return -1;
    }

    String totp(String secret, long step) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(decodeBase32(secret), "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(step).array());
            int offset = hash[hash.length - 1] & 15;
            int truncated = ((hash[offset] & 127) << 24) | ((hash[offset + 1] & 255) << 16)
                | ((hash[offset + 2] & 255) << 8) | (hash[offset + 3] & 255);
            return String.format(Locale.ROOT, "%06d", truncated % CODE_MODULUS);
        } catch (Exception exception) {
            throw new IllegalStateException("TOTP validation failed", exception);
        }
    }

    private byte[] decodeBase32(String secret) {
        ByteBuffer decoded = ByteBuffer.allocate(secret.length() * 5 / 8);
        int buffer = 0;
        int bits = 0;
        for (char character : secret.toCharArray()) {
            int value = BASE32.indexOf(character);
            if (value < 0) {
                throw new IllegalArgumentException("Invalid Base32 secret");
            }
            buffer = (buffer << 5) | value;
            bits += 5;
            if (bits >= 8) {
                bits -= 8;
                decoded.put((byte) (buffer >> bits));
            }
        }
        return decoded.array();
    }
}
