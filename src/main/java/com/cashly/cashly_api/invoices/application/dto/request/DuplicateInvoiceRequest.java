package com.cashly.cashly_api.invoices.application.dto.request;

import java.util.UUID;

public record DuplicateInvoiceRequest(UUID customerId) {
}
