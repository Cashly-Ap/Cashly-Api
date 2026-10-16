package com.cashly.cashly_api.purchases.application.cancel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelPurchaseRequest(
        @NotBlank(message = "Cancellation reason is required")
        @Size(max = 500, message = "Cancellation reason must be at most 500 characters")
        String reason
) {}
