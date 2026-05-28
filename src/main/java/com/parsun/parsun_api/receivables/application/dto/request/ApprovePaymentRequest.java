package com.parsun.parsun_api.receivables.application.dto.request;

import java.math.BigDecimal;

public record ApprovePaymentRequest(
        BigDecimal commissionPercentage
) {}
