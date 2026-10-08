package com.cashly.cashly_api.purchases.application.api;

import java.util.UUID;
import java.time.LocalDate;

public record PurchaseDocumentFilter(LocalDate dateFrom, LocalDate dateTo, UUID supplierId, String status, UUID productId, UUID categoryId) {}
