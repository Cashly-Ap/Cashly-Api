package com.cashly.cashly_api.payments.collection.application.reject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RejectPaymentRequest(

        @NotNull
        @NotBlank
        String reason

) {}
