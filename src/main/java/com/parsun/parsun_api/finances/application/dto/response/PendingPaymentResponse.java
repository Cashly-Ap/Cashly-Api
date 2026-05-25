package com.parsun.parsun_api.finances.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PendingPaymentResponse(
        UUID id,
        String paymentNumber,
        UUID customerId,
        String customerName,
        BigDecimal amount,
        String paymentMethodName,
        String status,
        LocalDate paymentDate,
        LocalDateTime createdAt,
        String invoiceNumber,
        String voucherPath,
        String approvalStatus,
        String rejectionReason,
        BigDecimal commissionPercentage
) {}
