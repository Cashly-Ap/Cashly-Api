package com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SupplierPaymentDecisionRequest(
        @NotBlank(message = "Reason is required") String reason) {
}
