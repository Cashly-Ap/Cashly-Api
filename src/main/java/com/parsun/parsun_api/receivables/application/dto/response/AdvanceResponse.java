package com.parsun.parsun_api.receivables.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdvanceResponse(
        String paymentNumber,
        BigDecimal amount,
        BigDecimal remainingAmount,
        LocalDate date
) {}
