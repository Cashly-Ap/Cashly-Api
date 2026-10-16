package com.cashly.cashly_api.purchases.reporting.application.dto.model;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseReportRow(UUID id, String purchaseNumber, String supplierName, String status, BigDecimal subtotal, LocalDateTime createdAt) {}
