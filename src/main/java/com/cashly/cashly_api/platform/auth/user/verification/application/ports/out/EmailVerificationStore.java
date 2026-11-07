package com.cashly.cashly_api.platform.auth.user.verification.application.ports.out;

import com.cashly.cashly_api.platform.auth.user.verification.domain.entity.VerificationToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationStore {

    void invalidateActive(UUID userId, Instant usedAt);

    void create(VerificationToken token);

    Optional<VerificationToken> findByHash(String tokenHash);

    boolean markUsed(UUID tokenId, Instant usedAt);

    Optional<Instant> lastIssuedAt(UUID userId);
}
