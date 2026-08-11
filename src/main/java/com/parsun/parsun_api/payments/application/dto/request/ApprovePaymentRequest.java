package com.parsun.parsun_api.payments.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApprovePaymentRequest(

        @NotNull
        @NotBlank
        String reason

) {}
