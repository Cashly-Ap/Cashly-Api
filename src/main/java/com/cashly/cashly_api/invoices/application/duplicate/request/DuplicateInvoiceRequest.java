package com.cashly.cashly_api.invoices.application.duplicate.request;

import java.util.UUID;

public record DuplicateInvoiceRequest(UUID customerId) {
}
