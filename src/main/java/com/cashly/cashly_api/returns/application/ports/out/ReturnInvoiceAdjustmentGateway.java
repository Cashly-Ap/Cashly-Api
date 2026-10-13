package com.cashly.cashly_api.returns.application.ports.out;

import com.cashly.cashly_api.returns.domain.entities.Return;

import java.math.BigDecimal;

public interface ReturnInvoiceAdjustmentGateway {
    void apply(Return returned);
    void reverse(Return returned, BigDecimal totalPaid, BigDecimal totalDebitNotes, BigDecimal totalCreditNotes);
}
