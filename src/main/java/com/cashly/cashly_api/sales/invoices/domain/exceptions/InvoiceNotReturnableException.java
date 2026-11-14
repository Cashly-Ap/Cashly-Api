package com.cashly.cashly_api.sales.invoices.domain.exceptions;

import com.cashly.cashly_api.sales.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.sales.invoices.domain.valueobjects.InvoiceStatus;

public class InvoiceNotReturnableException extends RuntimeException {

    public InvoiceNotReturnableException(InvoiceId id,
            InvoiceStatus currentStatus) {
        super("La factura " + id.value()
                + " no acepta devoluciones. Estado actual: " + currentStatus);
    }
}
