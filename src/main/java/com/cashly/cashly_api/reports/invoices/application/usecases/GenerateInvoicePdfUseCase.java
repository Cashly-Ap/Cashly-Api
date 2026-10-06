package com.cashly.cashly_api.reports.invoices.application.usecases;

import java.util.UUID;

public interface GenerateInvoicePdfUseCase {
    byte[] execute(UUID invoiceId);
}
