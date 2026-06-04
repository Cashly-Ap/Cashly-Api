package com.parsun.parsun_api.customers.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

public record CreateAdvanceRequest(
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 integer and 2 decimal digits")
    BigDecimal amount,
    String notes
) {}
