package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.integration;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.ports.out.PayablePurchaseGateway;
import com.cashly.cashly_api.procurement.purchases.domain.entity.Purchase;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalPayablePurchaseGateway implements PayablePurchaseGateway {

    private final PurchaseRepository purchases;

    public LocalPayablePurchaseGateway(PurchaseRepository purchases) {
        this.purchases = purchases;
    }

    @Override
    public List<PayablePurchase> findPayableBySupplier(UUID supplierId) {
        return purchases.findAllWithFilters(null, null, supplierId,
                List.of("CONFIRMED", "PARTIALLY_PAID"), null, null, "createdAt",
                "asc").stream().map(this::toPayable).toList();
    }

    @Override
    public Optional<PayablePurchase> findById(UUID purchaseId) {
        return purchases.findById(PurchaseId.of(purchaseId))
                .map(this::toPayable);
    }

    @Override
    public void applyPaidTotal(UUID purchaseId, BigDecimal totalPaid) {
        Purchase purchase = purchases.findById(PurchaseId.of(purchaseId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase not found: " + purchaseId));
        purchase.applyPayment(totalPaid);
        purchases.save(purchase);
    }

    private PayablePurchase toPayable(Purchase purchase) {
        return new PayablePurchase(purchase.getId().value(),
                purchase.getSupplierId(), purchase.getSubtotal(),
                purchase.getReturnedTotal());
    }
}
