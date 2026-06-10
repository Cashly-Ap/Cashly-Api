package com.parsun.parsun_api.cashregister.infrastructure.persistence;

import com.parsun.parsun_api.cashregister.application.dto.DailyTotals;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegister;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegisterDetail;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterStatus;
import com.parsun.parsun_api.cashregister.infrastructure.persistence.jpa.CashRegisterDetailJpaEntity;
import com.parsun.parsun_api.cashregister.infrastructure.persistence.jpa.CashRegisterJpaEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CashRegisterRepositoryAdapter implements CashRegisterRepository {

    private final CashRegisterJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public CashRegisterRepositoryAdapter(CashRegisterJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public CashRegister save(CashRegister closure) {
        var entity = toEntity(closure);
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CashRegister> findById(RegisterId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CashRegister> findByClosureDate(LocalDate closureDate) {
        return jpaRepository.findByClosureDate(closureDate).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CashRegister> findActiveClosure() {
        return jpaRepository.findActiveClosure().map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CashRegister> findAll(int page, int size, String sort) {
        var pageable = PageRequest.of(page, size);
        var entityPage = jpaRepository.findAll(pageable);
        var domains = entityPage.getContent().stream()
                .map(this::toDomain)
                .toList();
        return new PageImpl<>(domains, pageable, entityPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public DailyTotals findDailyTotals(LocalDate closureDate) {
        var paymentsTotal = queryDecimal(
                "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_date = :date AND status = 'APPLIED'",
                closureDate
        );
        var paymentsCount = queryInt(
                "SELECT COUNT(*) FROM payments WHERE payment_date = :date AND status = 'APPLIED'",
                closureDate
        );

        var advancesTotal = queryDecimal(
                "SELECT COALESCE(SUM(amount), 0) FROM balance_transactions WHERE type = 'ADVANCED' AND DATE(created_at) = :date",
                closureDate
        );
        var advancesCount = queryInt(
                "SELECT COUNT(*) FROM balance_transactions WHERE type = 'ADVANCED' AND DATE(created_at) = :date",
                closureDate
        );

        var returnsTotal = queryDecimal(
                "SELECT COALESCE(SUM(total), 0) FROM returns WHERE DATE(created_at) = :date",
                closureDate
        );
        var returnsCount = queryInt(
                "SELECT COUNT(*) FROM returns WHERE DATE(created_at) = :date",
                closureDate
        );

        var invoicesTotal = queryDecimal(
                "SELECT COALESCE(SUM(subtotal), 0) FROM invoices WHERE DATE(created_at) = :date AND status = 'PAID'",
                closureDate
        );
        var invoicesCount = queryInt(
                "SELECT COUNT(*) FROM invoices WHERE DATE(created_at) = :date AND status = 'PAID'",
                closureDate
        );

        var creditNotesTotal = queryDecimal(
                "SELECT COALESCE(SUM(amount), 0) FROM credit_notes WHERE DATE(created_at) = :date",
                closureDate
        );

        var debitNotesTotal = queryDecimal(
                "SELECT COALESCE(SUM(amount), 0) FROM debit_notes WHERE DATE(created_at) = :date",
                closureDate
        );

        var totalInvoiced = queryDecimal(
                "SELECT COALESCE(SUM(subtotal), 0) FROM invoices WHERE DATE(created_at) = :date",
                closureDate
        );

        var totalExpenses = returnsTotal.add(creditNotesTotal);

        List<?> rawRows = entityManager.createNativeQuery(
                        """
                        SELECT pm.id, pm.name,
                               COALESCE(SUM(p.amount), 0) AS total_amount,
                               COUNT(p.id) AS tx_count
                        FROM payment_methods pm
                        LEFT JOIN payments p ON p.payment_method_id = pm.id
                            AND p.payment_date = :date
                            AND p.status = 'APPLIED'
                        WHERE pm.is_active = true
                        GROUP BY pm.id, pm.name
                        ORDER BY pm.name
                        """
                )
                .setParameter("date", closureDate)
                .getResultList();

        var details = rawRows.stream()
                .map(row -> {
                    Object[] cols = (Object[]) row;
                    UUID pmId = toUuid(cols[0]);
                    String pmName = (String) cols[1];
                    BigDecimal totalAmount = ((Number) cols[2]) != null
                            ? BigDecimal.valueOf(((Number) cols[2]).doubleValue())
                            : BigDecimal.ZERO;
                    int txCount = ((Number) cols[3]) != null ? ((Number) cols[3]).intValue() : 0;
                    return CashRegisterDetail.create(pmId, pmName, txCount, totalAmount);
                })
                .collect(Collectors.toList());

        return new DailyTotals(
                paymentsTotal, paymentsCount,
                advancesTotal, advancesCount,
                returnsTotal, returnsCount,
                creditNotesTotal, debitNotesTotal,
                invoicesTotal, invoicesCount,
                totalInvoiced, paymentsTotal, totalExpenses,
                details
        );
    }

    private CashRegisterJpaEntity toEntity(CashRegister closure) {
        var entity = new CashRegisterJpaEntity();
        entity.setId(closure.getId().value());
        entity.setClosureDate(closure.getClosureDate());
        entity.setStatus(closure.getStatus().name());
        entity.setOpenedBy(closure.getOpenedBy());
        entity.setOpenedByName(closure.getOpenedByName());
        entity.setOpenedAt(closure.getOpenedAt());
        if (closure.getClosedBy() != null) entity.setClosedBy(closure.getClosedBy());
        if (closure.getClosedByName() != null) entity.setClosedByName(closure.getClosedByName());
        if (closure.getClosedAt() != null) entity.setClosedAt(closure.getClosedAt());
        if (closure.getVerifiedBy() != null) entity.setVerifiedBy(closure.getVerifiedBy());
        if (closure.getVerifiedByName() != null) entity.setVerifiedByName(closure.getVerifiedByName());
        if (closure.getVerifiedAt() != null) entity.setVerifiedAt(closure.getVerifiedAt());
        entity.setTotalPayments(closure.getTotalPayments());
        entity.setTotalAdvances(closure.getTotalAdvances());
        entity.setTotalReturns(closure.getTotalReturns());
        entity.setTotalCreditNotes(closure.getTotalCreditNotes());
        entity.setTotalDebitNotes(closure.getTotalDebitNotes());
        entity.setTotalInvoices(closure.getTotalInvoices());
        entity.setPaymentCount(closure.getPaymentCount());
        entity.setAdvanceCount(closure.getAdvanceCount());
        entity.setReturnCount(closure.getReturnCount());
        entity.setInvoiceCount(closure.getInvoiceCount());
        entity.setExpectedClosingBalance(closure.getExpectedClosingBalance());
        entity.setActualClosingBalance(closure.getActualClosingBalance());
        entity.setDifference(closure.getDifference());
        entity.setClosingNotes(closure.getClosingNotes());
        entity.setVersion(closure.getVersion());
        entity.setCreatedAt(closure.getCreatedAt());
        entity.setUpdatedAt(closure.getUpdatedAt());

        var details = closure.getDetails().stream()
                .map(d -> toDetailEntity(d, entity))
                .toList();
        entity.setDetails(details);

        return entity;
    }

    private CashRegisterDetailJpaEntity toDetailEntity(CashRegisterDetail detail, CashRegisterJpaEntity closureEntity) {
        var entity = new CashRegisterDetailJpaEntity();
        entity.setId(detail.getId());
        entity.setClosure(closureEntity);
        entity.setPaymentMethodId(detail.getPaymentMethodId());
        entity.setPaymentMethodName(detail.getPaymentMethodName());
        entity.setTransactionCount(detail.getTransactionCount());
        entity.setExpectedAmount(detail.getExpectedAmount());
        return entity;
    }

    private CashRegister toDomain(CashRegisterJpaEntity entity) {
        var details = entity.getDetails().stream()
                .map(d -> CashRegisterDetail.reconstitute(
                        d.getId(), d.getPaymentMethodId(), d.getPaymentMethodName(),
                        d.getTransactionCount(), d.getExpectedAmount()))
                .toList();

        return CashRegister.reconstitute(
                RegisterId.of(entity.getId()),
                entity.getClosureDate(),
                RegisterStatus.valueOf(entity.getStatus()),
                entity.getOpenedBy(),
                entity.getOpenedByName(),
                entity.getOpenedAt(),
                entity.getClosedBy(),
                entity.getClosedByName(),
                entity.getClosedAt(),
                entity.getVerifiedBy(),
                entity.getVerifiedByName(),
                entity.getVerifiedAt(),
                entity.getTotalPayments(),
                entity.getTotalAdvances(),
                entity.getTotalReturns(),
                entity.getTotalCreditNotes(),
                entity.getTotalDebitNotes(),
                entity.getTotalInvoices(),
                entity.getPaymentCount(),
                entity.getAdvanceCount(),
                entity.getReturnCount(),
                entity.getInvoiceCount(),
                entity.getExpectedClosingBalance(),
                entity.getActualClosingBalance(),
                entity.getDifference(),
                entity.getClosingNotes(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                details
        );
    }

    private BigDecimal queryDecimal(String sql, LocalDate date) {
        var result = entityManager.createNativeQuery(sql)
                .setParameter("date", date)
                .getSingleResult();
        return result != null ? BigDecimal.valueOf(((Number) result).doubleValue()) : BigDecimal.ZERO;
    }

    private int queryInt(String sql, LocalDate date) {
        var result = entityManager.createNativeQuery(sql)
                .setParameter("date", date)
                .getSingleResult();
        return result != null ? ((Number) result).intValue() : 0;
    }

    private UUID toUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID uuid) return uuid;
        if (value instanceof byte[] bytes) {
            long msb = 0, lsb = 0;
            for (int i = 0; i < 8; i++) msb = (msb << 8) | (bytes[i] & 0xff);
            for (int i = 8; i < 16; i++) lsb = (lsb << 8) | (bytes[i] & 0xff);
            return new UUID(msb, lsb);
        }
        if (value instanceof String s) return UUID.fromString(s);
        throw new IllegalArgumentException("Cannot convert to UUID: " + value.getClass());
    }
}
