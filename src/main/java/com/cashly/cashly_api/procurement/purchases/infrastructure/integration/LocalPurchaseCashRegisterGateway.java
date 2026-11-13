package com.cashly.cashly_api.procurement.purchases.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.ports.in.PaymentMethodDetailsQuery;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseCashRegisterGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LocalPurchaseCashRegisterGateway
        implements PurchaseCashRegisterGateway {

    private final CashMovementService movements;
    private final PaymentMethodDetailsQuery paymentMethods;

    public LocalPurchaseCashRegisterGateway(CashMovementService movements,
            PaymentMethodDetailsQuery paymentMethods) {
        this.movements = movements;
        this.paymentMethods = paymentMethods;
    }

    @Override
    public void recordCashPurchase(BigDecimal amount, UUID purchaseId,
            String purchaseNumber, UUID actorId, String actorName) {
        UUID paymentMethodId = paymentMethods.findByCode("CASH")
                .map(PaymentMethodDetailsQuery.PaymentMethodDetails::id)
                .orElse(null);
        movements.record(CashMovementDirection.OUT, CashMovementType.EXPENSE,
                amount, paymentMethodId,
                "Compra al contado " + purchaseNumber, "PURCHASE", purchaseId,
                actorId, actorName);
    }
}
