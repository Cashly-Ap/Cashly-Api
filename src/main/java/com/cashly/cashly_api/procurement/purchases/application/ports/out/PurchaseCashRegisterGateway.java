package com.cashly.cashly_api.procurement.purchases.application.ports.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface PurchaseCashRegisterGateway {

    void recordCashPurchase(BigDecimal amount, UUID purchaseId,
            String purchaseNumber, UUID actorId, String actorName);
}
