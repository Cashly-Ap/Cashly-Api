package com.parsun.parsun_api.auth.user.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ChangeCommissionDeductionRequest(
    @NotNull(message = "Commission deduction percentage is required")
    @DecimalMin(value = "0.00", message = "Commission deduction percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Commission deduction percentage cannot exceed 100")
    BigDecimal commissionDeductionPercentage
) {}
