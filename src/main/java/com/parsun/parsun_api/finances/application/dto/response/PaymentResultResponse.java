package com.parsun.parsun_api.finances.application.dto.response;

import java.util.UUID;

public record PaymentResultResponse(
        UUID id,
        String status,
        String message,
        boolean requiresValidation
) {}
