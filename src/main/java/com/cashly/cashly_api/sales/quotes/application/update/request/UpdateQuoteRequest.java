package com.cashly.cashly_api.sales.quotes.application.update.request;

import com.cashly.cashly_api.sales.quotes.application.create.request.CreateQuoteRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateQuoteRequest(
        LocalDate expiryDate,

        @Size(max = 1000, message = "Notes must be at most 1000 characters")
        String notes,

        @NotEmpty(message = "At least one quote line is required")
        @Valid
        List<CreateQuoteRequest.QuoteLineRequest> lines
) {}
