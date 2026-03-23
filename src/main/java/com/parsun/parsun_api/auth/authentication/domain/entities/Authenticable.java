package com.parsun.parsun_api.auth.authentication.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface Authenticable {

    String getSubjectId();

    String getEmail();

    String getHashedPassword();

    String getDisplayName();

    AuthenticableType getType();

    boolean canAuthenticate();

    boolean isActive();

    LocalDateTime getCreatedAt();

    default BigDecimal getCommissionDeductionPercentage() {
        return null;
    }
}
