package com.cashly.cashly_api.sales.quotes.application.cancel.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelQuoteRequest(
        @NotBlank(message = "Cancellation reason is required") @Size(max = 500, message = "Reason must be at most 500 characters") String reason) {
}
