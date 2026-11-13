package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.credit_notes.infrastructure.persistence.entities.CreditNoteEntity;
import com.cashly.cashly_api.finance.billing.debit_notes.infrastructure.persistence.entities.DebitNoteEntity;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnEntity;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.entities.CashMovementJpaEntity;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.entities.CashRegisterDetailJpaEntity;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.entities.CashRegisterJpaEntity;
import com.cashly.cashly_api.finance.treasury.payment_methods.infrastructure.persistence.entities.PaymentMethodEntity;
import com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.persistence.entities.PaymentEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.entities.BalanceTransactionEntity;
import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseEntity;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V25CashMovementsSeed implements SeedStep {

    private static final String ADMIN_NAME = "Cashly Administrator";

    private final EntityManager entityManager;

    public V25CashMovementsSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private record Method(UUID id, String name) {
    }

    private record MovementSpec(String suffix, String type, String direction,
            Method method, String amount, String description, String sourceType,
            UUID sourceId, boolean cancelled, LocalDateTime occurredAt) {
    }

    private record MethodTotal(String name, BigDecimal amount, int count) {
    }

    @Override
    public int version() {
        return 25;
    }

    @Override
    public void seed() {
        Map<UUID, String> methodNames = methodNames();
        for (CashRegisterJpaEntity register : registers()) {
            YearMonth month = YearMonth.from(register.getClosureDate());
            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();
            List<MovementSpec> specs = specifications(register, month, start,
                    end, methodNames);
            persistMovements(register, specs);
            applyTotals(register, register.getClosureDate().toString(), specs);
        }
    }

    private List<CashRegisterJpaEntity> registers() {
        return entityManager.createQuery(
                "SELECT r FROM CashRegisterJpaEntity r ORDER BY r.closureDate",
                CashRegisterJpaEntity.class).getResultList();
    }

    private Map<UUID, String> methodNames() {
        Map<UUID, String> names = new LinkedHashMap<>();
        entityManager.createQuery("SELECT m FROM PaymentMethodEntity m",
                PaymentMethodEntity.class).getResultList()
                .forEach(method -> names.put(method.getId(),
                        method.getName()));
        return names;
    }

    private List<MovementSpec> specifications(CashRegisterJpaEntity register,
            YearMonth month, LocalDateTime start, LocalDateTime end,
            Map<UUID, String> methodNames) {
        List<MovementSpec> specs = new ArrayList<>();
        invoices(start, end, specs);
        payments(start, end, methodNames, specs);
        notes(start, end, specs);
        returns(start, end, specs);
        balances(start, end, register, methodNames, specs);
        purchases(start, end, register, methodNames, specs);
        manual(month, register, methodNames, specs);
        return specs;
    }

    private void invoices(LocalDateTime start, LocalDateTime end,
            List<MovementSpec> specs) {
        List<InvoiceEntity> invoices = entityManager.createQuery(
                "SELECT i FROM InvoiceEntity i WHERE i.createdAt >= :start "
                        + "AND i.createdAt < :end AND i.status <> 'DRAFT' "
                        + "AND i.status <> 'CANCELLED' ORDER BY i.createdAt",
                InvoiceEntity.class).setParameter("start", start)
                .setParameter("end", end).getResultList();
        for (InvoiceEntity invoice : invoices) {
            specs.add(new MovementSpec("invoice-" + invoice.getId(), "INVOICE",
                    "IN", null, invoice.getSubtotal().toPlainString(),
                    "Factura " + invoice.getInvoiceNumber(), "INVOICE",
                    invoice.getId(), false, invoice.getCreatedAt()));
        }
    }

    private void payments(LocalDateTime start, LocalDateTime end,
            Map<UUID, String> methodNames, List<MovementSpec> specs) {
        List<PaymentEntity> payments = entityManager.createQuery(
                "SELECT p FROM PaymentEntity p WHERE p.createdAt >= :start "
                        + "AND p.createdAt < :end AND p.status = 'APPLIED' "
                        + "ORDER BY p.createdAt",
                PaymentEntity.class).setParameter("start", start)
                .setParameter("end", end).getResultList();
        for (PaymentEntity payment : payments) {
            Method method = method(payment.getPaymentMethodId(), methodNames);
            BigDecimal amount = payment.getAppliedAmount() != null
                    && payment.getAppliedAmount().signum() > 0
                            ? payment.getAppliedAmount()
                            : payment.getAmount();
            specs.add(new MovementSpec("payment-" + payment.getId(), "PAYMENT",
                    "IN", method, amount.toPlainString(),
                    "Pago " + payment.getPaymentNumber(), "PAYMENT",
                    payment.getId(), false, payment.getCreatedAt()));
        }
    }

    private void notes(LocalDateTime start, LocalDateTime end,
            List<MovementSpec> specs) {
        List<CreditNoteEntity> credits = entityManager.createQuery(
                "SELECT c FROM CreditNoteEntity c WHERE c.createdAt >= :start "
                        + "AND c.createdAt < :end ORDER BY c.createdAt",
                CreditNoteEntity.class).setParameter("start", start)
                .setParameter("end", end).getResultList();
        for (CreditNoteEntity note : credits) {
            specs.add(new MovementSpec("credit-note-" + note.getId(),
                    "CREDIT_NOTE", "OUT", null, note.getAmount().toPlainString(),
                    "Nota de crédito " + note.getNoteNumber(), "CREDIT_NOTE",
                    note.getId(), false, note.getCreatedAt()));
        }
        List<DebitNoteEntity> debits = entityManager.createQuery(
                "SELECT d FROM DebitNoteEntity d WHERE d.createdAt >= :start "
                        + "AND d.createdAt < :end ORDER BY d.createdAt",
                DebitNoteEntity.class).setParameter("start", start)
                .setParameter("end", end).getResultList();
        for (DebitNoteEntity note : debits) {
            specs.add(new MovementSpec("debit-note-" + note.getId(),
                    "DEBIT_NOTE", "IN", null, note.getAmount().toPlainString(),
                    "Nota de débito " + note.getNoteNumber(), "DEBIT_NOTE",
                    note.getId(), false, note.getCreatedAt()));
        }
    }

    private void returns(LocalDateTime start, LocalDateTime end,
            List<MovementSpec> specs) {
        List<ReturnEntity> returns = entityManager.createQuery(
                "SELECT r FROM ReturnEntity r WHERE r.createdAt >= :start "
                        + "AND r.createdAt < :end ORDER BY r.createdAt",
                ReturnEntity.class).setParameter("start", start)
                .setParameter("end", end).getResultList();
        for (ReturnEntity returned : returns) {
            specs.add(new MovementSpec("return-" + returned.getId(), "RETURN",
                    "OUT", null, returned.getTotal().toPlainString(),
                    "Devolución " + returned.getReturnNumber(), "RETURN",
                    returned.getId(), false, returned.getCreatedAt()));
        }
    }

    private void balances(LocalDateTime start, LocalDateTime end,
            CashRegisterJpaEntity register, Map<UUID, String> methodNames,
            List<MovementSpec> specs) {
        List<BalanceTransactionEntity> balances = entityManager.createQuery(
                "SELECT b FROM BalanceTransactionEntity b "
                        + "WHERE b.createdAt >= :start AND b.createdAt < :end "
                        + "AND b.type IN ('ADVANCED', 'REFUND') "
                        + "ORDER BY b.createdAt",
                BalanceTransactionEntity.class).setParameter("start", start)
                .setParameter("end", end).getResultList();
        Method cash = method(id("payment-method.cash"), methodNames);
        for (BalanceTransactionEntity balance : balances) {
            boolean advance = "ADVANCED".equals(balance.getType());
            specs.add(new MovementSpec(
                    (advance ? "advance-" : "refund-") + balance.getId(),
                    advance ? "ADVANCE" : "REFUND", advance ? "IN" : "OUT",
                    cash, balance.getAmount().toPlainString(),
                    advance ? "Anticipo de cliente" : "Reembolso de cliente",
                    "BALANCE_TRANSACTION", balance.getId(), false,
                    balance.getCreatedAt()));
        }
    }

    private void purchases(LocalDateTime start, LocalDateTime end,
            CashRegisterJpaEntity register, Map<UUID, String> methodNames,
            List<MovementSpec> specs) {
        List<PurchaseEntity> purchases = entityManager.createQuery(
                "SELECT p FROM PurchaseEntity p WHERE p.paymentType = 'CASH' "
                        + "AND p.status <> 'DRAFT' AND p.status <> 'CANCELLED' "
                        + "AND p.confirmedAt >= :start AND p.confirmedAt < :end "
                        + "ORDER BY p.confirmedAt",
                PurchaseEntity.class).setParameter("start", start)
                .setParameter("end", end).getResultList();
        Method cash = method(id("payment-method.cash"), methodNames);
        for (PurchaseEntity purchase : purchases) {
            specs.add(new MovementSpec("purchase-" + purchase.getId(),
                    "EXPENSE", "OUT", cash,
                    purchase.getSubtotal().toPlainString(),
                    "Compra al contado " + purchase.getPurchaseNumber(),
                    "PURCHASE", purchase.getId(), false,
                    purchase.getConfirmedAt()));
        }
    }

    private void manual(YearMonth month, CashRegisterJpaEntity register,
            Map<UUID, String> methodNames, List<MovementSpec> specs) {
        Method cash = method(id("payment-method.cash"), methodNames);
        specs.add(new MovementSpec("manual-income", "MANUAL_INCOME", "IN",
                cash, "5.00", "Ingreso manual de " + month, "MANUAL", null,
                false, month.atDay(10).atTime(12, 0)));
        specs.add(new MovementSpec("withdrawal", "WITHDRAWAL", "OUT", cash,
                "50.00", "Retiro al banco de " + month, "MANUAL", null, false,
                month.atEndOfMonth().atTime(16, 0)));
        boolean incoming = month.getMonthValue() % 2 == 0;
        specs.add(new MovementSpec("adjustment", "ADJUSTMENT",
                incoming ? "IN" : "OUT", cash, "0.50",
                "Ajuste por redondeo de " + month, "MANUAL", null, false,
                month.atDay(15).atTime(13, 0)));
        if (month.getMonthValue() % 3 == 0) {
            specs.add(new MovementSpec("refund", "REFUND", "OUT", cash, "10.00",
                    "Reembolso a cliente de " + month, "MANUAL", null, false,
                    month.atDay(20).atTime(11, 0)));
        }
        if (month.getYear() == 2026 && month.getMonthValue() == 1) {
            specs.add(new MovementSpec("cancelled-expense", "EXPENSE", "OUT",
                    cash, "25.00", "Gasto anulado en revisión", "MANUAL", null,
                    true, month.atDay(20).atTime(14, 0)));
        }
    }

    private Method method(UUID id, Map<UUID, String> methodNames) {
        if (id == null) {
            return null;
        }
        return new Method(id, methodNames.getOrDefault(id, id.toString()));
    }

    private void persistMovements(CashRegisterJpaEntity register,
            List<MovementSpec> specs) {
        String registerKey = register.getClosureDate().toString();
        int index = 0;
        for (MovementSpec spec : specs) {
            UUID movementId = id("cash-movement." + registerKey + "."
                    + spec.suffix());
            if (entityManager.find(CashMovementJpaEntity.class,
                    movementId) == null) {
                entityManager.persist(
                        movement(register, spec, movementId, index));
            }
            index++;
        }
    }

    private CashMovementJpaEntity movement(CashRegisterJpaEntity register,
            MovementSpec spec, UUID movementId, int index) {
        var movement = new CashMovementJpaEntity();
        movement.setId(movementId);
        movement.setRegisterId(register.getId());
        movement.setDirection(spec.direction());
        movement.setType(spec.type());
        movement.setAmount(new BigDecimal(spec.amount()));
        movement.setPaymentMethodId(
                spec.method() == null ? null : spec.method().id());
        movement.setPaymentMethodName(
                spec.method() == null ? null : spec.method().name());
        movement.setDescription(spec.description());
        movement.setSourceType(spec.sourceType());
        movement.setSourceId(spec.sourceId());
        movement.setCreatedBy(ADMIN_USER);
        movement.setCreatedByName(ADMIN_NAME);
        LocalDateTime occurredAt = spec.occurredAt() != null
                ? spec.occurredAt()
                : register.getClosureDate().atTime(8, 30)
                        .plusMinutes(index * 15L);
        movement.setOccurredAt(occurredAt);
        movement.setCancelled(spec.cancelled());
        if (spec.cancelled()) {
            movement.setCancellationReason("Movimiento de prueba anulado");
            movement.setCancelledAt(occurredAt.plusHours(1));
            movement.setCancelledBy(ADMIN_USER);
            movement.setCancelledByName(ADMIN_NAME);
        }
        return movement;
    }

    private void applyTotals(CashRegisterJpaEntity register, String registerKey,
            List<MovementSpec> movements) {
        BigDecimal payments = BigDecimal.ZERO;
        BigDecimal advances = BigDecimal.ZERO;
        BigDecimal otherIncome = BigDecimal.ZERO;
        BigDecimal refunds = BigDecimal.ZERO;
        BigDecimal otherExpense = BigDecimal.ZERO;
        BigDecimal invoices = BigDecimal.ZERO;
        BigDecimal debitNotes = BigDecimal.ZERO;
        BigDecimal creditNotes = BigDecimal.ZERO;
        BigDecimal returns = BigDecimal.ZERO;
        int paymentCount = 0;
        int advanceCount = 0;
        int returnCount = 0;
        int invoiceCount = 0;
        Map<UUID, BigDecimal> cashByMethod = new LinkedHashMap<>();
        Map<UUID, String> nameByMethod = new LinkedHashMap<>();
        Map<UUID, Integer> countByMethod = new LinkedHashMap<>();

        for (MovementSpec spec : movements) {
            if (spec.cancelled()) {
                continue;
            }
            BigDecimal amount = new BigDecimal(spec.amount());
            CashMovementType type = CashMovementType.valueOf(spec.type());
            boolean income = CashMovementDirection.IN.name()
                    .equals(spec.direction());
            switch (type) {
                case PAYMENT -> {
                    payments = payments.add(amount);
                    paymentCount++;
                }
                case ADVANCE -> {
                    advances = advances.add(amount);
                    advanceCount++;
                }
                case INVOICE -> {
                    invoices = invoices.add(amount);
                    invoiceCount++;
                }
                case DEBIT_NOTE -> debitNotes = debitNotes.add(amount);
                case CREDIT_NOTE -> creditNotes = creditNotes.add(amount);
                case RETURN -> {
                    returns = returns.add(amount);
                    returnCount++;
                }
                case REFUND -> refunds = refunds.add(amount);
                case EXPENSE, WITHDRAWAL ->
                    otherExpense = otherExpense.add(amount);
                case MANUAL_INCOME -> otherIncome = otherIncome.add(amount);
                case ADJUSTMENT -> {
                    if (income) {
                        otherIncome = otherIncome.add(amount);
                    } else {
                        otherExpense = otherExpense.add(amount);
                    }
                }
            }
            if (type.affectsCashBalance() && spec.method() != null) {
                BigDecimal signed = income ? amount : amount.negate();
                UUID methodId = spec.method().id();
                cashByMethod.merge(methodId, signed, BigDecimal::add);
                nameByMethod.putIfAbsent(methodId, spec.method().name());
                countByMethod.merge(methodId, 1, Integer::sum);
            }
        }

        BigDecimal income = payments.add(advances).add(otherIncome);
        BigDecimal expense = refunds.add(otherExpense);
        BigDecimal expected = register.getOpeningBalance().add(income)
                .subtract(expense);
        boolean closed = "CLOSED".equals(register.getStatus());
        BigDecimal targetDifference = closed ? register.getDifference() : null;

        register.setTotalPayments(payments);
        register.setPaymentCount(paymentCount);
        register.setTotalAdvances(advances);
        register.setAdvanceCount(advanceCount);
        register.setTotalReturns(returns);
        register.setReturnCount(returnCount);
        register.setTotalCreditNotes(creditNotes);
        register.setTotalDebitNotes(debitNotes);
        register.setTotalInvoices(invoices);
        register.setInvoiceCount(invoiceCount);
        register.setTotalIncome(income);
        register.setTotalExpense(expense);
        register.setExpectedClosingBalance(expected);

        register.getDetails().clear();
        entityManager.flush();
        CashRegisterDetailJpaEntity differenceDetail = null;
        BigDecimal largestExpected = BigDecimal.ZERO.negate();
        for (Map.Entry<UUID, BigDecimal> entry : cashByMethod.entrySet()) {
            UUID methodId = entry.getKey();
            BigDecimal expectedAmount = entry.getValue().max(BigDecimal.ZERO);
            var detail = new CashRegisterDetailJpaEntity();
            detail.setId(id("cash-register-detail." + registerKey + "."
                    + methodId));
            detail.setClosure(register);
            detail.setPaymentMethodId(methodId);
            detail.setPaymentMethodName(
                    nameByMethod.getOrDefault(methodId, methodId.toString()));
            detail.setTransactionCount(
                    countByMethod.getOrDefault(methodId, 0));
            detail.setExpectedAmount(expectedAmount);
            detail.setActualAmount(closed ? expectedAmount : null);
            detail.setDifference(closed ? BigDecimal.ZERO : null);
            register.getDetails().add(detail);
            if (expectedAmount.compareTo(largestExpected) > 0) {
                largestExpected = expectedAmount;
                differenceDetail = detail;
            }
        }
        if (closed && targetDifference != null) {
            register.setDifference(targetDifference);
            register.setActualClosingBalance(expected.add(targetDifference));
            if (differenceDetail != null) {
                differenceDetail.setActualAmount(
                        differenceDetail.getExpectedAmount()
                                .add(targetDifference));
                differenceDetail.setDifference(targetDifference);
            }
        } else {
            register.setDifference(null);
            register.setActualClosingBalance(null);
        }
        register.setUpdatedAt(register.getClosedAt() != null
                ? register.getClosedAt()
                : LocalDateTime.now());
    }
}
