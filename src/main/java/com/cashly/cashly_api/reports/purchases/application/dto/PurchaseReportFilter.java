package com.cashly.cashly_api.reports.purchases.application.dto;

import java.util.UUID;
import java.time.LocalDate;

public record PurchaseReportFilter(LocalDate dateFrom, LocalDate dateTo, UUID supplierId, String status, UUID productId, UUID categoryId) {}
