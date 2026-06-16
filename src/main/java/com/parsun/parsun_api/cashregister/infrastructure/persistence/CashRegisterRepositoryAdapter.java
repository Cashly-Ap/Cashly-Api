package com.parsun.parsun_api.cashregister.infrastructure.persistence;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterExportRow;
import com.parsun.parsun_api.cashregister.application.dto.DailyTotals;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegister;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegisterDetail;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterStatus;
import com.parsun.parsun_api.cashregister.infrastructure.persistence.jpa.CashRegisterDetailJpaEntity;
import com.parsun.parsun_api.cashregister.infrastructure.persistence.jpa.CashRegisterJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public Page<CashRegister> findAll(int page, int size, String sort,
                                      LocalDate dateFrom, LocalDate dateTo) {
        Specification<CashRegisterJpaEntity> spec = withDateBetween(dateFrom, dateTo);
        var pageable = PageRequest.of(page, size, parseSort(sort));
        var entityPage = jpaRepository.findAll(spec, pageable);
        var domains = entityPage.getContent().stream()
                .map(this::toDomain)
                .toList();
        return new PageImpl<>(domains, pageable, entityPage.getTotalElements());
    }

    private Specification<CashRegisterJpaEntity> withDateBetween(LocalDate dateFrom, LocalDate dateTo) {
        return (root, query, cb) -> {
            if (dateFrom == null && dateTo == null) return null;
            var path = root.<LocalDate>get("closureDate");
            if (dateFrom != null && dateTo != null) return cb.between(path, dateFrom, dateTo);
            if (dateFrom != null) return cb.greaterThanOrEqualTo(path, dateFrom);
            return cb.lessThanOrEqualTo(path, dateTo);
        };
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.unsorted();
        String[] parts = sort.split(",");
        if (parts.length < 2) return Sort.by(parts[0].trim());
        return Sort.by(Sort.Direction.fromString(parts[1].trim()), parts[0].trim());
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

        var refundsTotal = queryDecimal(
                "SELECT COALESCE(ABS(SUM(amount)), 0) FROM balance_transactions WHERE type = 'REFUND' AND DATE(created_at) = :date",
                closureDate
        );
        var refundsCount = queryInt(
                "SELECT COUNT(*) FROM balance_transactions WHERE type = 'REFUND' AND DATE(created_at) = :date",
                closureDate
        );

        returnsTotal = returnsTotal.add(refundsTotal);
        returnsCount = returnsCount + refundsCount;

        var invoicesTotal = queryDecimal(
                "SELECT COALESCE(SUM(subtotal), 0) FROM invoices WHERE DATE(created_at) = :date AND status != 'DRAFT' AND status != 'CANCELLED'",
                closureDate
        );
        var invoicesCount = queryInt(
                "SELECT COUNT(*) FROM invoices WHERE DATE(created_at) = :date AND status != 'DRAFT' AND status != 'CANCELLED'",
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
                invoicesTotal, paymentsTotal, totalExpenses,
                details
        );
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<CashRegisterExportRow> findExportRows(LocalDate dateFrom, LocalDate dateTo) {
        List<CashRegisterExportRow> rows = new ArrayList<>();
        int sortOrder = 0;

        rows.addAll(queryInvoices(dateFrom, dateTo, sortOrder));
        sortOrder += 1000;
        rows.addAll(queryReturns(dateFrom, dateTo, sortOrder));
        sortOrder += 1000;
        rows.addAll(queryRefunds(dateFrom, dateTo, sortOrder));
        sortOrder += 1000;
        rows.addAll(queryPayments(dateFrom, dateTo, sortOrder));
        sortOrder += 1000;
        rows.addAll(queryAdvances(dateFrom, dateTo, sortOrder));
        sortOrder += 1000;
        rows.addAll(queryCreditNotes(dateFrom, dateTo, sortOrder));
        sortOrder += 1000;
        rows.addAll(queryDebitNotes(dateFrom, dateTo, sortOrder));

        return rows;
    }

    private List<CashRegisterExportRow> queryInvoices(LocalDate dateFrom, LocalDate dateTo, int baseOrder) {
        String sql = """
                SELECT i.invoice_number, i.subtotal, i.created_at, i.customer_name
                FROM invoices i
                WHERE DATE(i.created_at) BETWEEN :dateFrom AND :dateTo AND i.status = 'PAID'
                ORDER BY i.created_at
                """;
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        List<Object[]> results = query.getResultList();
        List<CashRegisterExportRow> rows = new ArrayList<>();
        int order = baseOrder;
        for (Object[] row : results) {
            String docNumber = (String) row[0];
            BigDecimal total = toBigDecimal(row[1]);
            LocalDate emissionDate = toLocalDate(row[2]);
            String customerName = (String) row[3];
            var paymentBreakdown = getInvoicePaymentBreakdown(docNumber, dateFrom, dateTo);
            rows.add(CashRegisterExportRow.of(
                    emissionDate, "FAC", docNumber, total,
                    paymentBreakdown.efectivo(),
                    paymentBreakdown.dolar(),
                    paymentBreakdown.transferencia(),
                    paymentBreakdown.zelle(),
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    customerName, "VENTAS", order++
            ));
        }
        return rows;
    }

    private List<CashRegisterExportRow> queryReturns(LocalDate dateFrom, LocalDate dateTo, int baseOrder) {
        String sql = """
                SELECT r.return_number, r.total, r.created_at, c.legal_name
                FROM returns r
                JOIN customers c ON c.id = r.customer_id
                WHERE DATE(r.created_at) BETWEEN :dateFrom AND :dateTo
                ORDER BY r.created_at
                """;
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        List<Object[]> results = query.getResultList();
        List<CashRegisterExportRow> rows = new ArrayList<>();
        int order = baseOrder;
        for (Object[] row : results) {
            String docNumber = (String) row[0];
            BigDecimal total = toBigDecimal(row[1]);
            LocalDate emissionDate = toLocalDate(row[2]);
            String customerName = (String) row[3];
            rows.add(CashRegisterExportRow.of(
                    emissionDate, "DEV", docNumber, total,
                    total, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    customerName, "VENTAS", order++
            ));
        }
        return rows;
    }

    private List<CashRegisterExportRow> queryRefunds(LocalDate dateFrom, LocalDate dateTo, int baseOrder) {
        String sql = """
                SELECT bt.number, bt.amount, bt.created_at, c.legal_name
                FROM balance_transactions bt
                JOIN customers c ON c.id = bt.customer_id
                WHERE DATE(bt.created_at) BETWEEN :dateFrom AND :dateTo AND bt.type = 'REFUND'
                ORDER BY bt.created_at
                """;
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        List<Object[]> results = query.getResultList();
        List<CashRegisterExportRow> rows = new ArrayList<>();
        int order = baseOrder;
        for (Object[] row : results) {
            String docNumber = (String) row[0];
            BigDecimal total = toBigDecimal(row[1]).abs();
            LocalDate emissionDate = toLocalDate(row[2]);
            String customerName = (String) row[3];
            rows.add(CashRegisterExportRow.of(
                    emissionDate, "REI", docNumber, total,
                    total, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    customerName, "OPERACIONES DE CAJA", order++
            ));
        }
        return rows;
    }

    private List<CashRegisterExportRow> queryPayments(LocalDate dateFrom, LocalDate dateTo, int baseOrder) {
        String sql = """
                SELECT p.payment_number, p.amount, p.payment_date, c.legal_name, pm.code
                FROM payments p
                JOIN customers c ON c.id = p.customer_id
                JOIN payment_methods pm ON pm.id = p.payment_method_id
                WHERE p.payment_date BETWEEN :dateFrom AND :dateTo AND p.status = 'APPLIED'
                ORDER BY p.created_at
                """;
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        List<Object[]> results = query.getResultList();
        List<CashRegisterExportRow> rows = new ArrayList<>();
        int order = baseOrder;
        for (Object[] row : results) {
            String docNumber = (String) row[0];
            BigDecimal total = toBigDecimal(row[1]);
            LocalDate emissionDate = toLocalDate(row[2]);
            String customerName = (String) row[3];
            String pmCode = (String) row[4];
            var breakdown = distributeByPaymentMethod(total, pmCode);
            rows.add(CashRegisterExportRow.of(
                    emissionDate, "PAG", docNumber, total,
                    breakdown.efectivo(), breakdown.dolar(),
                    breakdown.transferencia(), breakdown.zelle(),
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    customerName, "CUENTAS POR COBRAR", order++
            ));
        }
        return rows;
    }

    private List<CashRegisterExportRow> queryAdvances(LocalDate dateFrom, LocalDate dateTo, int baseOrder) {
        String sql = """
                SELECT bt.number, bt.amount, bt.created_at, c.legal_name
                FROM balance_transactions bt
                JOIN customers c ON c.id = bt.customer_id
                WHERE DATE(bt.created_at) BETWEEN :dateFrom AND :dateTo AND bt.type = 'ADVANCED'
                ORDER BY bt.created_at
                """;
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        List<Object[]> results = query.getResultList();
        List<CashRegisterExportRow> rows = new ArrayList<>();
        int order = baseOrder;
        for (Object[] row : results) {
            String docNumber = (String) row[0];
            BigDecimal total = toBigDecimal(row[1]);
            LocalDate emissionDate = toLocalDate(row[2]);
            String customerName = (String) row[3];
            rows.add(CashRegisterExportRow.of(
                    emissionDate, "ANT", docNumber, total,
                    total, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    customerName, "CUENTAS POR COBRAR", order++
            ));
        }
        return rows;
    }

    private List<CashRegisterExportRow> queryCreditNotes(LocalDate dateFrom, LocalDate dateTo, int baseOrder) {
        String sql = """
                SELECT cn.note_number, cn.amount, cn.created_at, c.legal_name
                FROM credit_notes cn
                JOIN customers c ON c.id = cn.customer_id
                WHERE DATE(cn.created_at) BETWEEN :dateFrom AND :dateTo
                ORDER BY cn.created_at
                """;
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        List<Object[]> results = query.getResultList();
        List<CashRegisterExportRow> rows = new ArrayList<>();
        int order = baseOrder;
        for (Object[] row : results) {
            String docNumber = (String) row[0];
            BigDecimal total = toBigDecimal(row[1]);
            LocalDate emissionDate = toLocalDate(row[2]);
            String customerName = (String) row[3];
            rows.add(CashRegisterExportRow.of(
                    emissionDate, "NC", docNumber, total,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, total,
                    customerName, "OPERACIONES DE CAJA", order++
            ));
        }
        return rows;
    }

    private List<CashRegisterExportRow> queryDebitNotes(LocalDate dateFrom, LocalDate dateTo, int baseOrder) {
        String sql = """
                SELECT dn.note_number, dn.amount, dn.created_at, c.legal_name
                FROM debit_notes dn
                JOIN customers c ON c.id = dn.customer_id
                WHERE DATE(dn.created_at) BETWEEN :dateFrom AND :dateTo
                ORDER BY dn.created_at
                """;
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        List<Object[]> results = query.getResultList();
        List<CashRegisterExportRow> rows = new ArrayList<>();
        int order = baseOrder;
        for (Object[] row : results) {
            String docNumber = (String) row[0];
            BigDecimal total = toBigDecimal(row[1]);
            LocalDate emissionDate = toLocalDate(row[2]);
            String customerName = (String) row[3];
            rows.add(CashRegisterExportRow.of(
                    emissionDate, "ND", docNumber, total,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    customerName, "OPERACIONES DE CAJA", order++
            ));
        }
        return rows;
    }

    private PaymentBreakdown getInvoicePaymentBreakdown(String invoiceNumber, LocalDate dateFrom, LocalDate dateTo) {
        String sql = """
                SELECT pm.code, COALESCE(SUM(pa.allocated_amount), 0)
                FROM invoices i
                JOIN payment_allocations pa ON pa.invoice_id = i.id
                JOIN payments p ON p.id = pa.payment_id
                JOIN payment_methods pm ON pm.id = p.payment_method_id
                WHERE i.invoice_number = :invoiceNumber
                  AND p.status = 'APPLIED'
                GROUP BY pm.code
                """;
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("invoiceNumber", invoiceNumber);
        List<Object[]> results = query.getResultList();
        BigDecimal efectivo = BigDecimal.ZERO;
        BigDecimal dolar = BigDecimal.ZERO;
        BigDecimal transferencia = BigDecimal.ZERO;
        BigDecimal zelle = BigDecimal.ZERO;
        for (Object[] row : results) {
            String code = (String) row[0];
            BigDecimal amount = toBigDecimal(row[1]);
            switch (code) {
                case "PM-0001" -> efectivo = efectivo.add(amount);
                case "PM-0006" -> dolar = dolar.add(amount);
                case "PM-0002" -> transferencia = transferencia.add(amount);
                case "PM-0008" -> zelle = zelle.add(amount);
                default -> efectivo = efectivo.add(amount);
            }
        }
        return new PaymentBreakdown(efectivo, dolar, transferencia, zelle);
    }

    private PaymentBreakdown distributeByPaymentMethod(BigDecimal total, String pmCode) {
        return switch (pmCode) {
            case "PM-0001" -> new PaymentBreakdown(total, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            case "PM-0006" -> new PaymentBreakdown(BigDecimal.ZERO, total, BigDecimal.ZERO, BigDecimal.ZERO);
            case "PM-0002" -> new PaymentBreakdown(BigDecimal.ZERO, BigDecimal.ZERO, total, BigDecimal.ZERO);
            case "PM-0008" -> new PaymentBreakdown(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, total);
            default -> new PaymentBreakdown(total, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        };
    }

    private record PaymentBreakdown(BigDecimal efectivo, BigDecimal dolar,
                                    BigDecimal transferencia, BigDecimal zelle) {}

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof Date sqlDate) return sqlDate.toLocalDate();
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime().toLocalDate();
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof LocalDateTime ldt) return ldt.toLocalDate();
        return null;
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
