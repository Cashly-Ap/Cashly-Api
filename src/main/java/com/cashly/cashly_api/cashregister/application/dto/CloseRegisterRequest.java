package com.cashly.cashly_api.cashregister.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CloseRegisterRequest(
        @NotBlank String closedByName,
        @NotNull @DecimalMin("0.00") BigDecimal declaredBalance,
        String closingNotes
) {}
