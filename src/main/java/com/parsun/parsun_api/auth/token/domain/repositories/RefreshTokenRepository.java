package com.parsun.parsun_api.auth.token.domain.repositories;

import java.util.List;
import java.util.Optional;

import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findBySubjectId(String subjectId);

    List<RefreshToken> findByUserId(UserId userId);

    void revokeAllBySubjectId(String subjectId);

    void revokeAllByUserId(UserId userId);

    void deleteExpiredTokens();
}
