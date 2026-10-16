package com.cashly.cashly_api.orders.reporting.application.dto.model;

import java.util.UUID;
import java.time.LocalDate;

public record OrderReportFilter(LocalDate dateFrom, LocalDate dateTo, UUID customerId, String status, UUID productId, UUID categoryId) {}
