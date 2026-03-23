package com.parsun.parsun_api.auth.user.application.dto.response;

import com.parsun.parsun_api.auth.user.domain.entities.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserResponse(
    String id,
    String email,
    String displayName,
    boolean active,
    boolean emailVerified,
    BigDecimal commissionDeductionPercentage,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId().getValue().toString(),
            user.getEmail().getValue(),
            user.getUserName().getValue(),
            user.isActive(),
            user.isEmailVerified(),
            user.getCommissionDeductionPercentage().value(),
            user.getCreatedAt()
        );
    }
}
