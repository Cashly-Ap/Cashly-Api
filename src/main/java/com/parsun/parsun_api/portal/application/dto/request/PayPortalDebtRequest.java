package com.parsun.parsun_api.portal.application.dto.request;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
public record PayPortalDebtRequest(

    UUID paymentMethodId,

    @Size(max = 100, message = "Reference number must be at most 100 characters")
    String referenceNumber,

    LocalDate paymentDate,

    BigDecimal amount,

    Boolean useBalance,

    BigDecimal balanceAmount

) {}