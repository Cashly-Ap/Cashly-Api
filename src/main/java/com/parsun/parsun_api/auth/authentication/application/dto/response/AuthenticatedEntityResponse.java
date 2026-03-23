package com.parsun.parsun_api.auth.authentication.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;

public record AuthenticatedEntityResponse(
    String id,
    String email,
    String displayName,
    AuthenticableType type,
    boolean active,
    BigDecimal commissionDeductionPercentage,
    LocalDateTime createdAt
) {
    public static AuthenticatedEntityResponse from(Authenticable authenticable) {
        return new AuthenticatedEntityResponse(
            authenticable.getSubjectId(),
            authenticable.getEmail(),
            authenticable.getDisplayName(),
            authenticable.getType(),
            authenticable.isActive(),
            authenticable.getCommissionDeductionPercentage(),
            authenticable.getCreatedAt()
        );
    }
}
