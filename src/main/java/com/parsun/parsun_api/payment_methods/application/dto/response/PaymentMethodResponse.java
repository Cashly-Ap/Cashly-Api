package com.parsun.parsun_api.payment_methods.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentMethodResponse(
        UUID id,
        String code,
        String name,
        String description,
        boolean isActive,
        boolean hasAssociatedRecords,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
