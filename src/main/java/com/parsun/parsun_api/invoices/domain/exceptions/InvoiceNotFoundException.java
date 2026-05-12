package com.parsun.parsun_api.invoices.domain.exceptions;

import java.util.UUID;

public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(UUID id) {
        super("Invoice not found with id: " + id);
    }
}
