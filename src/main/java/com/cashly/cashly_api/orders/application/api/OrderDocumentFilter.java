package com.cashly.cashly_api.orders.application.api;

import java.util.UUID;
import java.time.LocalDate;

public record OrderDocumentFilter(LocalDate dateFrom, LocalDate dateTo, UUID customerId, String status, UUID productId, UUID categoryId) {}
