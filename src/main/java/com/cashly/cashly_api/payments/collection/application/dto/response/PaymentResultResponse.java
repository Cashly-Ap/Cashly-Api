package com.cashly.cashly_api.payments.collection.application.dto.response;

import java.util.UUID;

public record PaymentResultResponse(
        UUID id,
        String status,
        String message,
        boolean requiresValidation
) {}
