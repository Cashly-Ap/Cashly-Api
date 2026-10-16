package com.cashly.cashly_api.auth.token.domain;

import com.cashly.cashly_api.auth.authentication.domain.AuthenticableType;
import com.cashly.cashly_api.auth.token.domain.valueobjects.RefreshTokenId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public class RefreshToken {
    private final RefreshTokenId id;
    private final String subjectId;
    private final AuthenticableType subjectType;
    private final String token;
    private LocalDateTime expiresAt;
    private boolean revoked;
    private LocalDateTime createdAt;

    public RefreshToken(RefreshTokenId id, String subjectId, AuthenticableType subjectType,
                        String token, LocalDateTime expiresAt) {
        validateParameters(id, subjectId, subjectType, token, expiresAt);

        this.id = id;
        this.subjectId = subjectId;
        this.subjectType = subjectType;
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.createdAt = LocalDateTime.now();
    }

    @Deprecated
    public RefreshToken(RefreshTokenId id, UserId userId, String token, LocalDateTime expiresAt) {
        this(id, userId.getValue().toString(), AuthenticableType.USER, token, expiresAt);
    }

    private void validateParameters(RefreshTokenId id, String subjectId, AuthenticableType subjectType,
                                    String token, LocalDateTime expiresAt) {
        if (id == null) {
            throw new IllegalArgumentException("RefreshToken ID cannot be null");
        }
        if (subjectId == null || subjectId.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject ID cannot be null or empty");
        }
        if (subjectType == null) {
            throw new IllegalArgumentException("Subject type cannot be null");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("Expiration date cannot be null");
        }
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expiration date cannot be in the past");
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean belongsToSubject(String subjectId) {
        if (subjectId == null) {
            return false;
        }
        return this.subjectId.equals(subjectId);
    }

    @Deprecated
    public boolean belongsToUser(UserId userId) {
        if (userId == null) {
            return false;
        }
        return this.subjectId.equals(userId.getValue().toString()) &&
               this.subjectType == AuthenticableType.USER;
    }

    @Deprecated
    public UserId getUserId() {
        if (subjectType != AuthenticableType.USER) {
            throw new IllegalStateException("This token does not belong to a User");
        }
        return UserId.from(subjectId);
    }

    public RefreshTokenId getId() {
        return id;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public AuthenticableType getSubjectType() {
        return subjectType;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RefreshToken that = (RefreshToken) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", subjectId=" + subjectId +
                ", subjectType=" + subjectType +
                ", token='***'" +
                ", expiresAt=" + expiresAt +
                ", revoked=" + revoked +
                ", createdAt=" + createdAt +
                '}';
    }
}
