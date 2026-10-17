package com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response;

import java.util.UUID;

public record PaymentResultResponse(
        UUID id,
        String status,
        String message,
        boolean requiresValidation
) {}
