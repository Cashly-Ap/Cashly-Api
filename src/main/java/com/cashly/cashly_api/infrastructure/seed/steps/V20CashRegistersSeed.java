package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.entities.CashRegisterDetailJpaEntity;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.entities.CashRegisterJpaEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class V20CashRegistersSeed implements SeedStep {
    private final EntityManager entityManager;
    public V20CashRegistersSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 20;
    }
    @Override
    public void seed() {
        register("closed", LocalDate.of(2025, 1, 10), "CLOSED", true, "13.00");
        register("open", LocalDate.of(2025, 1, 13), "OPEN", false, "0.00");
    }
    private void register(String key, LocalDate date, String status,
            boolean closed, String payments) {
        var registerId = id("cash-register." + key);
        if (entityManager.find(CashRegisterJpaEntity.class, registerId) != null)
            return;
        var amount = new BigDecimal(payments);
        var register = new CashRegisterJpaEntity();
        register.setId(registerId);
        register.setClosureDate(date);
        register.setStatus(status);
        register.setOpenedBy(ADMIN_USER);
        register.setOpenedByName("Cashly Administrator");
        register.setOpenedAt(date.atTime(8, 0));
        register.setOpeningBalance(new BigDecimal("100.00"));
        register.setTotalPayments(amount);
        register.setTotalAdvances(BigDecimal.ZERO);
        register.setTotalReturns(BigDecimal.ZERO);
        register.setTotalCreditNotes(BigDecimal.ZERO);
        register.setTotalDebitNotes(BigDecimal.ZERO);
        register.setTotalInvoices(amount);
        register.setPaymentCount(closed ? 2 : 0);
        register.setAdvanceCount(0);
        register.setReturnCount(0);
        register.setInvoiceCount(closed ? 2 : 0);
        register.setExpectedClosingBalance(
                new BigDecimal("100.00").add(amount));
        register.setTotalIncome(amount);
        register.setTotalExpense(BigDecimal.ZERO);
        register.setCreatedAt(date.atTime(8, 0));
        register.setUpdatedAt(date.atTime(closed ? 17 : 8, 0));
        if (closed) {
            register.setClosedBy(ADMIN_USER);
            register.setClosedByName("Cashly Administrator");
            register.setClosedAt(date.atTime(17, 0));
            register.setActualClosingBalance(
                    register.getExpectedClosingBalance());
            register.setDifference(BigDecimal.ZERO);
            register.setClosingNotes("Deterministic seed closure");
        }
        var detail = new CashRegisterDetailJpaEntity();
        detail.setId(id("cash-register-detail." + key));
        detail.setClosure(register);
        detail.setPaymentMethodId(id("payment-method.cash"));
        detail.setPaymentMethodName("Cash");
        detail.setTransactionCount(closed ? 1 : 0);
        detail.setExpectedAmount(amount);
        if (closed) {
            detail.setActualAmount(amount);
            detail.setDifference(BigDecimal.ZERO);
        }
        register.getDetails().add(detail);
        entityManager.persist(register);
    }
}
