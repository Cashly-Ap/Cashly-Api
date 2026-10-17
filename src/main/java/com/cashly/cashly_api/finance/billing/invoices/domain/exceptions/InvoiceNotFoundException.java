package com.cashly.cashly_api.finance.billing.invoices.domain.exceptions;

import java.util.UUID;

public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(UUID id) {
        super("Invoice not found with id: " + id);
    }
}
