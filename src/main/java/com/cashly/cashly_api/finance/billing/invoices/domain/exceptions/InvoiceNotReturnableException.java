package com.cashly.cashly_api.finance.billing.invoices.domain.exceptions;

import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;

public class InvoiceNotReturnableException extends RuntimeException {

    public InvoiceNotReturnableException(InvoiceId id,
            InvoiceStatus currentStatus) {
        super("La factura " + id.value()
                + " no acepta devoluciones. Estado actual: " + currentStatus);
    }
}
