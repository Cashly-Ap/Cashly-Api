package com.parsun.parsun_api.receivables.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectPaymentRequest(
        @NotBlank(message = "Rejection reason is required")
        String reason
) {}
