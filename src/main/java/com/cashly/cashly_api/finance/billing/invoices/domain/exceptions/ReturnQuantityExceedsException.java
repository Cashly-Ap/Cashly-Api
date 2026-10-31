package com.cashly.cashly_api.finance.billing.invoices.domain.exceptions;

import java.math.BigDecimal;
import java.util.UUID;

public class ReturnQuantityExceedsException extends RuntimeException {

    public ReturnQuantityExceedsException(UUID invoiceLineId,
            BigDecimal attempted, BigDecimal max) {
        super("La cantidad devuelta (" + attempted
                + ") excede la cantidad original (" + max + ") de la línea "
                + invoiceLineId);
    }
}
