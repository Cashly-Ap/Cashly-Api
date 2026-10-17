package com.cashly.cashly_api.platform.auth.token.domain.repository;

import java.util.List;
import java.util.Optional;

import com.cashly.cashly_api.platform.auth.token.domain.entity.RefreshToken;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findBySubjectId(String subjectId);

    List<RefreshToken> findByUserId(UserId userId);

    void revokeAllBySubjectId(String subjectId);

    void revokeAllByUserId(UserId userId);

    void deleteExpiredTokens();
}
