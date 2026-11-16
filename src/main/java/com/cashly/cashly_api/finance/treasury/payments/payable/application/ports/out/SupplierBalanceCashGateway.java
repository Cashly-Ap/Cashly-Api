package com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface SupplierBalanceCashGateway {

    void requireOpenRegister();

    void record(String direction, String type, BigDecimal amount,
            UUID paymentMethodId, String description, String sourceType,
            UUID sourceId, UUID actorId, String actorName);
}
