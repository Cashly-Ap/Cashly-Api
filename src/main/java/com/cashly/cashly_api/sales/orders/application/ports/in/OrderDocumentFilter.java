package com.cashly.cashly_api.sales.orders.application.ports.in;

import java.util.UUID;
import java.time.LocalDate;

public record OrderDocumentFilter(LocalDate dateFrom, LocalDate dateTo, UUID customerId, String status, UUID productId, UUID categoryId) {}
