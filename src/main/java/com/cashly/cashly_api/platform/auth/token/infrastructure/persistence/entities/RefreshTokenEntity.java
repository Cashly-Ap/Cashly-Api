package com.cashly.cashly_api.platform.auth.token.infrastructure.persistence.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.token.domain.entity.RefreshToken;
import com.cashly.cashly_api.platform.auth.token.domain.valueobjects.RefreshTokenId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_subject_id", columnList = "subject_id"),
    @Index(name = "idx_subject_type", columnList = "subject_type"),
    @Index(name = "idx_token", columnList = "token"),
    @Index(name = "idx_expires_at", columnList = "expires_at"),
    @Index(name = "idx_revoked", columnList = "revoked")
})
public class RefreshTokenEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @Column(name = "subject_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID subjectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", length = 20, nullable = false)
    private AuthenticableType subjectType;

    @Column(name = "token", length = 500, nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public RefreshTokenEntity() {
    }

    public static RefreshTokenEntity fromDomain(RefreshToken refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("RefreshToken cannot be null");
        }

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.id = refreshToken.getId().getValue();
        entity.subjectId = UUID.fromString(refreshToken.getSubjectId());
        entity.subjectType = refreshToken.getSubjectType();
        entity.token = refreshToken.getToken();
        entity.expiresAt = refreshToken.getExpiresAt();
        entity.revoked = refreshToken.isRevoked();
        entity.createdAt = refreshToken.getCreatedAt();

        return entity;
    }

    public void updateFromDomain(RefreshToken refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("RefreshToken cannot be null");
        }

        this.revoked = refreshToken.isRevoked();
    }

    public RefreshToken toDomain() {
        RefreshTokenId tokenId = new RefreshTokenId(this.id);

        LocalDateTime safeExpiresAt = this.expiresAt.isBefore(LocalDateTime.now())
            ? LocalDateTime.now().plusDays(1)
            : this.expiresAt;

        RefreshToken refreshToken = new RefreshToken(
            tokenId,
            this.subjectId.toString(),
            this.subjectType,
            this.token,
            safeExpiresAt
        );

        refreshToken.setRevoked(this.revoked);
        refreshToken.setCreatedAt(this.createdAt);
        refreshToken.setExpiresAt(this.expiresAt);

        return refreshToken;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }

    public AuthenticableType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(AuthenticableType subjectType) {
        this.subjectType = subjectType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RefreshTokenEntity that = (RefreshTokenEntity) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "RefreshTokenEntity{" +
                "id='" + id + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", subjectType=" + subjectType +
                ", token='***'" +
                ", expiresAt=" + expiresAt +
                ", revoked=" + revoked +
                ", createdAt=" + createdAt +
                '}';
    }
}
