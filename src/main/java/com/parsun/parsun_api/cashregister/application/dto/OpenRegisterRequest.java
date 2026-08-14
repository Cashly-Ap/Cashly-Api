package com.parsun.parsun_api.cashregister.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OpenRegisterRequest(
        @NotBlank String openedByName,
        @NotNull @DecimalMin("0.00") BigDecimal openingBalance
) {}
