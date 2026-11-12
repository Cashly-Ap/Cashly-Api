package com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface SupplierPaymentCashRegisterGateway {

    void recordOutgoing(LocalDate date, BigDecimal amount, UUID paymentMethodId,
            String description, UUID sourceId, UUID actorId,
            String actorName);
}
