package com.cashly.cashly_api.platform.auth.user.verification.domain.services;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationTokensTest {

    @Test
    void generateProducesUniqueUrlSafeTokens() {
        Set<String> tokens = new HashSet<>();
        for (int index = 0; index < 200; index++) {
            String token = VerificationTokens.generate();
            assertThat(token).matches("[A-Za-z0-9_-]+");
            tokens.add(token);
        }
        assertThat(tokens).hasSize(200);
    }

    @Test
    void hashIsStableAndHexEncoded() {
        String hash = VerificationTokens.hash("token");

        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
        assertThat(VerificationTokens.hash("token")).isEqualTo(hash);
        assertThat(VerificationTokens.hash("other")).isNotEqualTo(hash);
    }
}
