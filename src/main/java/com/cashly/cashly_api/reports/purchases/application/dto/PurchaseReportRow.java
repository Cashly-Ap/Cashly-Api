package com.cashly.cashly_api.reports.purchases.application.dto;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseReportRow(UUID id, String purchaseNumber, String supplierName, String status, BigDecimal subtotal, LocalDateTime createdAt) {}
