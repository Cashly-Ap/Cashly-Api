package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.ADMIN_USER;
import static com.cashly.cashly_api.infrastructure.seed.SeedIds.id;

import com.cashly.cashly_api.finance.treasury.payments.payable.domain.valueobjects.SupplierPaymentStatus;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierBalanceEntity;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierBalanceTransactionEntity;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierPaymentAllocationEntity;
import com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities.SupplierPaymentEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V31SupplierPaymentsSeed implements SeedStep {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1,
            16, 8, 0);

    private final EntityManager entityManager;

    public V31SupplierPaymentsSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public int version() {
        return 31;
    }

    @Override
    public void seed() {
        payment("one", "SPAY-2026-0001", id("supplier.one"),
                id("purchase.one"), "20.00");
        payment("two", "SPAY-2026-0002", id("supplier.two"),
                id("purchase.two"), "5.00");
        balance(id("supplier.one"), new BigDecimal("5.00"), "SBT-0001");
    }

    private void payment(String key, String number, UUID supplierId,
            UUID purchaseId, String amountValue) {
        UUID paymentId = id("supplier-payment." + key);
        if (entityManager.find(SupplierPaymentEntity.class, paymentId) != null) {
            return;
        }
        BigDecimal amount = new BigDecimal(amountValue);
        var payment = new SupplierPaymentEntity();
        payment.setId(paymentId);
        payment.setPaymentNumber(number);
        payment.setSupplierId(supplierId);
        payment.setAmount(amount);
        payment.setAppliedAmount(amount);
        payment.setPaymentMethodId(id("payment-method.cash"));
        payment.setSource("PAYMENT_METHOD");
        payment.setReferenceNumber("SEED-" + number);
        payment.setPaymentDate(LocalDate.of(2026, 1, 16));
        payment.setNotes("Deterministic seed supplier payment");
        payment.setStatus(SupplierPaymentStatus.APPLIED.name());
        payment.setCreatedBy(ADMIN_USER);
        payment.setCreatedByName("Cashly Administrator");
        payment.setCreatedAt(CREATED_AT);
        payment.setUpdatedAt(CREATED_AT);
        payment.setVerifiedBy(ADMIN_USER);
        payment.setVerifiedByName("Cashly Administrator");
        payment.setVerifiedAt(CREATED_AT);
        entityManager.persist(payment);

        var allocation = new SupplierPaymentAllocationEntity();
        allocation.setId(id("supplier-payment-allocation." + key));
        allocation.setPaymentId(paymentId);
        allocation.setPurchaseId(purchaseId);
        allocation.setAllocatedAmount(amount);
        allocation.setCreatedAt(CREATED_AT);
        entityManager.persist(allocation);
    }

    private void balance(UUID supplierId, BigDecimal amount, String number) {
        UUID balanceId = id("supplier-balance." + supplierId);
        if (entityManager.find(SupplierBalanceEntity.class, balanceId) == null) {
            var balance = new SupplierBalanceEntity();
            balance.setId(balanceId);
            balance.setSupplierId(supplierId);
            balance.setTotalBalance(amount);
            balance.setCreatedAt(CREATED_AT);
            balance.setUpdatedAt(CREATED_AT);
            entityManager.persist(balance);
        }
        UUID transactionId = id("supplier-balance-transaction." + supplierId);
        if (entityManager.find(SupplierBalanceTransactionEntity.class,
                transactionId) == null) {
            var transaction = new SupplierBalanceTransactionEntity();
            transaction.setId(transactionId);
            transaction.setSupplierId(supplierId);
            transaction.setType("ADVANCED");
            transaction.setAmount(amount);
            transaction.setReferenceType("SUPPLIER_PAYMENT");
            transaction.setNumber(number);
            transaction.setReferenceNumber(number);
            transaction.setCreatedBy(ADMIN_USER);
            transaction.setCreatedAt(CREATED_AT);
            entityManager.persist(transaction);
        }
    }
}
