package com.cashly.cashly_api.payments.collection.application.update.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdatePaymentRequest(

        String referenceNumber,

        LocalDate paymentDate,

        UUID paymentMethodId,

        @Size(max = 1000, message = "Notes must be at most 1000 characters")
        String notes

) {}
