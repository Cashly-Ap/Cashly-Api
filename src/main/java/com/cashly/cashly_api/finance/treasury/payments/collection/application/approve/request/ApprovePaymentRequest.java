package com.cashly.cashly_api.finance.treasury.payments.collection.application.approve.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApprovePaymentRequest(

        @NotNull
        @NotBlank
        String reason

) {}
