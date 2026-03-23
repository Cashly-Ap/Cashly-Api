package com.parsun.parsun_api.auth.token.domain.repositories;

import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(UserId userId);

    void revokeAllByUserId(UserId userId);

    void deleteExpiredTokens();
}
