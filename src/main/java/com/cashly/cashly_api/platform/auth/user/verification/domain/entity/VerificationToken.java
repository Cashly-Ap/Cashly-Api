package com.cashly.cashly_api.platform.auth.user.verification.domain.entity;

import java.time.Instant;
import java.util.UUID;

public record VerificationToken(UUID id, UUID userId, String tokenHash,
        Instant expiresAt, Instant usedAt) {

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }
}
