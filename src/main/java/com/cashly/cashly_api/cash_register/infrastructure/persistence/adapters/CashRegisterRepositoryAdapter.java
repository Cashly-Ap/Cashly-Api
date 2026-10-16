package com.cashly.cashly_api.cash_register.infrastructure.persistence.adapters;

import com.cashly.cashly_api.cash_register.sessions.infrastructure.persistence.repositories.CashMovementJpaRepository;
import com.cashly.cashly_api.cash_register.sessions.infrastructure.persistence.repositories.CashRegisterJpaRepository;

import com.cashly.cashly_api.cash_register.reporting.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cash_register.reporting.application.dto.CashRegisterReportFilter;
import com.cashly.cashly_api.cash_register.reporting.application.dto.CustomerSummary;
import com.cashly.cashly_api.cash_register.sessions.application.dto.DailyTotals;
import com.cashly.cashly_api.cash_register.sessions.domain.CashRegister;
import com.cashly.cashly_api.cash_register.sessions.domain.CashRegisterDetail;
import com.cashly.cashly_api.cash_register.sessions.domain.CashMovement;
import com.cashly.cashly_api.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;
import com.cashly.cashly_api.cash_register.sessions.domain.CashRegisterRepository;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.RegisterId;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.RegisterStatus;
import com.cashly.cashly_api.cash_register.sessions.infrastructure.persistence.entities.CashRegisterDetailJpaEntity;
import com.cashly.cashly_api.cash_register.sessions.infrastructure.persistence.entities.CashRegisterJpaEntity;
import com.cashly.cashly_api.cash_register.sessions.infrastructure.persistence.entities.CashMovementJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CashRegisterRepositoryAdapter implements CashRegisterRepository, CashRegisterQueryGateway {

    private final CashRegisterJpaRepository jpaRepository;
    private final EntityManager entityManager;
    private final CashMovementJpaRepository movementJpaRepository;

    @Autowired
    public CashRegisterRepositoryAdapter(CashRegisterJpaRepository jpaRepository,
                                         EntityManager entityManager,
                                         CashMovementJpaRepository movementJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
        this.movementJpaRepository = movementJpaRepository;
    }

    public CashRegisterRepositoryAdapter(CashRegisterJpaRepository jpaRepository, EntityManager entityManager) {
        this(jpaRepository, entityManager, null);
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
    public PageResponse<CashRegister> findAll(int page, int size, String sort,
                                      LocalDate dateFrom, LocalDate dateTo) {
        Specification<CashRegisterJpaEntity> spec = withDateBetween(dateFrom, dateTo);
        var pageable = PageRequest.of(page, size, parseSort(sort));
        var entityPage = jpaRepository.findAll(spec, pageable);
        var domains = entityPage.getContent().stream()
                .map(this::toDomain)
                .toList();
        return PageResponseMapper.from(new PageImpl<>(domains, pageable, entityPage.getTotalElements()));
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

        var totalExpenses = refundsTotal;
        var totalRevenue = paymentsTotal.add(advancesTotal);
        var totalInvoiced = invoicesTotal.add(debitNotesTotal).subtract(creditNotesTotal).subtract(returnsTotal);

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
                totalInvoiced, totalRevenue, totalExpenses,
                details
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DailyTotals findSessionTotals(RegisterId registerId) {
        List<CashMovement> movements = findMovements(registerId);
        BigDecimal payments = sum(movements, CashMovementDirection.IN, CashMovementType.PAYMENT);
        BigDecimal advances = sum(movements, CashMovementDirection.IN, CashMovementType.ADVANCE);
        BigDecimal otherIncome = movements.stream()
                .filter(m -> m.getDirection() == CashMovementDirection.IN)
                .filter(m -> m.getType().affectsCashBalance())
                .filter(m -> m.getType() != CashMovementType.PAYMENT && m.getType() != CashMovementType.ADVANCE)
                .map(CashMovement::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal refunds = movements.stream()
                .filter(m -> m.getDirection() == CashMovementDirection.OUT)
                .filter(m -> m.getType() == CashMovementType.REFUND)
                .map(CashMovement::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal otherExpense = movements.stream()
                .filter(m -> m.getDirection() == CashMovementDirection.OUT)
                .filter(m -> m.getType().affectsCashBalance())
                .filter(m -> m.getType() != CashMovementType.REFUND)
                .map(CashMovement::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal income = payments.add(advances).add(otherIncome);
        BigDecimal expense = refunds.add(otherExpense);
        BigDecimal invoices = sum(movements, CashMovementDirection.IN, CashMovementType.INVOICE);
        BigDecimal debitNotes = sum(movements, CashMovementDirection.IN, CashMovementType.DEBIT_NOTE);
        BigDecimal creditNotes = sum(movements, CashMovementDirection.OUT, CashMovementType.CREDIT_NOTE);
        BigDecimal returns = sum(movements, CashMovementDirection.OUT, CashMovementType.RETURN);
        BigDecimal netInvoiced = invoices.add(debitNotes).subtract(creditNotes).subtract(returns);

        Map<UUID, List<CashMovement>> byMethod = movements.stream()
                .filter(m -> m.getPaymentMethodId() != null)
                .filter(m -> m.getType().affectsCashBalance())
                .collect(Collectors.groupingBy(CashMovement::getPaymentMethodId, LinkedHashMap::new, Collectors.toList()));
        List<CashRegisterDetail> details = byMethod.values().stream().map(items -> {
            CashMovement first = items.getFirst();
            BigDecimal amount = items.stream()
                    .map(m -> m.getDirection() == CashMovementDirection.IN ? m.getAmount() : m.getAmount().negate())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return CashRegisterDetail.create(first.getPaymentMethodId(), first.getPaymentMethodName(),
                    items.size(), amount.max(BigDecimal.ZERO));
        }).toList();

        return new DailyTotals(payments, count(movements, CashMovementType.PAYMENT), advances,
                count(movements, CashMovementType.ADVANCE), returns,
                count(movements, CashMovementType.RETURN), creditNotes, debitNotes,
                invoices, count(movements, CashMovementType.INVOICE), netInvoiced, income, expense, details);
    }

    private BigDecimal sum(List<CashMovement> movements, CashMovementDirection direction,
                           CashMovementType type) {
        return movements.stream().filter(m -> m.getDirection() == direction && m.getType() == type)
                .map(CashMovement::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int count(List<CashMovement> movements, CashMovementType type) {
        return Math.toIntExact(movements.stream().filter(m -> m.getType() == type).count());
    }

    @Override
    @Transactional
    public CashMovement saveMovement(CashMovement movement) {
        if (movementJpaRepository == null) throw new IllegalStateException("Movement repository unavailable");
        return toMovementDomain(movementJpaRepository.save(toMovementEntity(movement)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashMovement> findMovements(RegisterId registerId) {
        if (movementJpaRepository == null) return List.of();
        return movementJpaRepository.findByRegisterIdOrderByOccurredAtAsc(registerId.value()).stream()
                .map(this::toMovementDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsMovement(String sourceType, UUID sourceId) {
        return sourceId != null && movementJpaRepository != null
                && movementJpaRepository.existsBySourceTypeAndSourceId(sourceType, sourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public DailyTotals findRangeTotals(LocalDate dateFrom, LocalDate dateTo) {
        var paymentsTotal = queryDecimalRange(
                "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_date BETWEEN :dateFrom AND :dateTo AND status = 'APPLIED'",
                dateFrom, dateTo
        );
        var paymentsCount = queryIntRange(
                "SELECT COUNT(*) FROM payments WHERE payment_date BETWEEN :dateFrom AND :dateTo AND status = 'APPLIED'",
                dateFrom, dateTo
        );
        var advancesTotal = queryDecimalRange(
                "SELECT COALESCE(SUM(amount), 0) FROM balance_transactions WHERE type = 'ADVANCED' AND DATE(created_at) BETWEEN :dateFrom AND :dateTo",
                dateFrom, dateTo
        );
        var advancesCount = queryIntRange(
                "SELECT COUNT(*) FROM balance_transactions WHERE type = 'ADVANCED' AND DATE(created_at) BETWEEN :dateFrom AND :dateTo",
                dateFrom, dateTo
        );
        var returnsTotal = queryDecimalRange(
                "SELECT COALESCE(SUM(total), 0) FROM returns WHERE DATE(created_at) BETWEEN :dateFrom AND :dateTo",
                dateFrom, dateTo
        );
        var returnsCount = queryIntRange(
                "SELECT COUNT(*) FROM returns WHERE DATE(created_at) BETWEEN :dateFrom AND :dateTo",
                dateFrom, dateTo
        );
        var refundsTotal = queryDecimalRange(
                "SELECT COALESCE(ABS(SUM(amount)), 0) FROM balance_transactions WHERE type = 'REFUND' AND DATE(created_at) BETWEEN :dateFrom AND :dateTo",
                dateFrom, dateTo
        );
        var refundsCount = queryIntRange(
                "SELECT COUNT(*) FROM balance_transactions WHERE type = 'REFUND' AND DATE(created_at) BETWEEN :dateFrom AND :dateTo",
                dateFrom, dateTo
        );
        returnsCount = returnsCount + refundsCount;

        var invoicesTotal = queryDecimalRange(
                "SELECT COALESCE(SUM(subtotal), 0) FROM invoices WHERE DATE(created_at) BETWEEN :dateFrom AND :dateTo AND status != 'DRAFT' AND status != 'CANCELLED'",
                dateFrom, dateTo
        );
        var invoicesCount = queryIntRange(
                "SELECT COUNT(*) FROM invoices WHERE DATE(created_at) BETWEEN :dateFrom AND :dateTo AND status != 'DRAFT' AND status != 'CANCELLED'",
                dateFrom, dateTo
        );
        var creditNotesTotal = queryDecimalRange(
                "SELECT COALESCE(SUM(amount), 0) FROM credit_notes WHERE DATE(created_at) BETWEEN :dateFrom AND :dateTo",
                dateFrom, dateTo
        );
        var debitNotesTotal = queryDecimalRange(
                "SELECT COALESCE(SUM(amount), 0) FROM debit_notes WHERE DATE(created_at) BETWEEN :dateFrom AND :dateTo",
                dateFrom, dateTo
        );

        var totalExpenses = refundsTotal;
        var totalRevenue = paymentsTotal.add(advancesTotal);
        var totalInvoiced = invoicesTotal.add(debitNotesTotal).subtract(creditNotesTotal).subtract(returnsTotal);

        var paymentsSummary = findPaymentMethodSummary(dateFrom, dateTo);

        return new DailyTotals(
                paymentsTotal, paymentsCount,
                advancesTotal, advancesCount,
                returnsTotal, returnsCount,
                creditNotesTotal, debitNotesTotal,
                invoicesTotal, invoicesCount,
                totalInvoiced, totalRevenue, totalExpenses,
                paymentsSummary
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashRegisterDetail> findPaymentMethodSummary(LocalDate dateFrom, LocalDate dateTo) {
        List<?> rawRows = entityManager.createNativeQuery(
                """
                SELECT pm.id, pm.name,
                       COALESCE(SUM(p.amount), 0) AS total_amount,
                       COUNT(p.id) AS tx_count
                FROM payment_methods pm
                LEFT JOIN payments p ON p.payment_method_id = pm.id
                    AND p.payment_date BETWEEN :dateFrom AND :dateTo
                    AND p.status = 'APPLIED'
                WHERE pm.is_active = true
                GROUP BY pm.id, pm.name
                ORDER BY pm.name
                """
        )
                .setParameter("dateFrom", dateFrom)
                .setParameter("dateTo", dateTo)
                .getResultList();

        return rawRows.stream()
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
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<CustomerSummary> findCustomerSummary(LocalDate dateFrom, LocalDate dateTo) {
        var invoicesMap = queryCustomerDecimalMap(
                "SELECT i.customer_id, c.legal_name, COALESCE(SUM(i.subtotal), 0) " +
                "FROM invoices i JOIN customers c ON c.id = i.customer_id " +
                "WHERE DATE(i.created_at) BETWEEN :dateFrom AND :dateTo AND i.status NOT IN ('DRAFT', 'CANCELLED') " +
                "GROUP BY i.customer_id, c.legal_name",
                dateFrom, dateTo
        );
        var paymentsMap = queryCustomerDecimalMap(
                "SELECT p.customer_id, c.legal_name, COALESCE(SUM(p.amount), 0) " +
                "FROM payments p JOIN customers c ON c.id = p.customer_id " +
                "WHERE p.payment_date BETWEEN :dateFrom AND :dateTo AND p.status = 'APPLIED' " +
                "GROUP BY p.customer_id, c.legal_name",
                dateFrom, dateTo
        );
        var returnsMap = queryCustomerDecimalMap(
                "SELECT r.customer_id, c.legal_name, COALESCE(SUM(r.total), 0) " +
                "FROM returns r JOIN customers c ON c.id = r.customer_id " +
                "WHERE DATE(r.created_at) BETWEEN :dateFrom AND :dateTo " +
                "GROUP BY r.customer_id, c.legal_name",
                dateFrom, dateTo
        );

        var allCustomers = new LinkedHashMap<String, AbstractMap.SimpleEntry<BigDecimal, BigDecimal>>();
        invoicesMap.forEach((name, amount) -> allCustomers.put(name, new AbstractMap.SimpleEntry<>(amount, BigDecimal.ZERO)));
        paymentsMap.forEach((name, amount) -> allCustomers.merge(name, new AbstractMap.SimpleEntry<>(BigDecimal.ZERO, amount),
                (a, b) -> new AbstractMap.SimpleEntry<>(a.getKey().add(b.getKey()), a.getValue().add(b.getValue()))));

        return allCustomers.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    BigDecimal invoiced = invoicesMap.getOrDefault(name, BigDecimal.ZERO);
                    BigDecimal paid = paymentsMap.getOrDefault(name, BigDecimal.ZERO);
                    BigDecimal returned = returnsMap.getOrDefault(name, BigDecimal.ZERO);
                    BigDecimal balance = paid.subtract(returned);
                    return new CustomerSummary(name, invoiced, paid, returned, balance);
                })
                .toList();
    }

    private Map<String, BigDecimal> queryCustomerDecimalMap(String sql, LocalDate dateFrom, LocalDate dateTo) {
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        List<Object[]> results = query.getResultList();
        var map = new java.util.LinkedHashMap<String, BigDecimal>();
        for (Object[] row : results) {
            String name = (String) row[1];
            BigDecimal amount = toBigDecimal(row[2]);
            map.merge(name, amount, BigDecimal::add);
        }
        return map;
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

    @Override
    @Transactional(readOnly = true)
    public List<CashRegisterExportRow> findExportRows(CashRegisterReportFilter filter) {
        List<CashRegisterExportRow> rows = findExportRows(filter.dateFrom(), filter.dateTo());
        if (!filter.hasCommercialFilters()) {
            return rows;
        }
        Set<String> allowedDocuments = findAllowedReportDocuments(filter);
        return rows.stream()
                .filter(row -> allowedDocuments.contains(row.documentNumber()))
                .toList();
    }

    private Set<String> findAllowedReportDocuments(CashRegisterReportFilter filter) {
        Set<String> documents = new java.util.HashSet<>();
        String lineFilter = reportLineFilter(filter, "i");
        addDocumentNumbers(documents, """
                SELECT DISTINCT i.invoice_number
                FROM invoices i
                WHERE i.status NOT IN ('DRAFT', 'CANCELLED')
                """ + reportCustomerFilter(filter, "i") + lineFilter, filter);
        addDocumentNumbers(documents, """
                SELECT DISTINCT r.return_number
                FROM returns r JOIN invoices i ON i.id = r.invoice_id
                WHERE 1 = 1
                """ + reportCustomerFilter(filter, "r") + lineFilter, filter);
        addDocumentNumbers(documents, """
                SELECT DISTINCT cn.note_number
                FROM credit_notes cn JOIN invoices i ON i.id = cn.invoice_id
                WHERE 1 = 1
                """ + reportCustomerFilter(filter, "cn") + lineFilter, filter);
        addDocumentNumbers(documents, """
                SELECT DISTINCT dn.note_number
                FROM debit_notes dn JOIN invoices i ON i.id = dn.invoice_id
                WHERE 1 = 1
                """ + reportCustomerFilter(filter, "dn") + lineFilter, filter);
        addDocumentNumbers(documents, """
                SELECT DISTINCT p.payment_number
                FROM payments p
                JOIN payment_allocations pa ON pa.payment_id = p.id
                JOIN invoices i ON i.id = pa.invoice_id
                WHERE p.status = 'APPLIED'
                """ + reportCustomerFilter(filter, "p") + lineFilter, filter);
        if (filter.productId() == null && filter.categoryId() == null) {
            addDocumentNumbers(documents, """
                    SELECT DISTINCT bt.number
                    FROM balance_transactions bt
                    WHERE bt.type IN ('ADVANCED', 'REFUND')
                    """ + reportCustomerFilter(filter, "bt"), filter);
        }
        return documents;
    }

    private String reportCustomerFilter(CashRegisterReportFilter filter, String alias) {
        return filter.customerId() == null ? "" : " AND " + alias + ".customer_id = :customerId";
    }

    private String reportLineFilter(CashRegisterReportFilter filter, String invoiceAlias) {
        if (filter.productId() == null && filter.categoryId() == null) {
            return "";
        }
        StringBuilder condition = new StringBuilder("""
                 AND EXISTS (
                    SELECT 1 FROM invoice_lines il
                    JOIN products pr ON pr.id = il.product_id
                    WHERE il.invoice_id = %s.id
                """.formatted(invoiceAlias));
        if (filter.productId() != null) condition.append(" AND il.product_id = :productId");
        if (filter.categoryId() != null) condition.append(" AND pr.category_id = :categoryId");
        return condition.append(")").toString();
    }

    private void addDocumentNumbers(Set<String> target, String sql, CashRegisterReportFilter filter) {
        var query = entityManager.createNativeQuery(sql);
        if (filter.customerId() != null) query.setParameter("customerId", filter.customerId());
        if (filter.productId() != null) query.setParameter("productId", filter.productId());
        if (filter.categoryId() != null) query.setParameter("categoryId", filter.categoryId());
        for (Object result : query.getResultList()) {
            if (result != null) target.add(result.toString());
        }
    }

    private List<CashRegisterExportRow> queryInvoices(LocalDate dateFrom, LocalDate dateTo, int baseOrder) {
        String sql = """
                SELECT i.invoice_number, i.subtotal, COALESCE(cm.occurred_at, i.updated_at), i.customer_name
                FROM invoices i
                LEFT JOIN cash_register_movements cm
                    ON cm.source_type = 'INVOICE' AND cm.source_id = i.id
                WHERE DATE(COALESCE(cm.occurred_at, i.updated_at)) BETWEEN :dateFrom AND :dateTo
                  AND i.status NOT IN ('DRAFT', 'CANCELLED')
                ORDER BY COALESCE(cm.occurred_at, i.updated_at)
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
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
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
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
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
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
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
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
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
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
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
                    BigDecimal.ZERO, total, BigDecimal.ZERO,
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
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
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
        entity.setOpeningBalance(closure.getOpeningBalance());
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
        entity.setTotalIncome(closure.getTotalIncome());
        entity.setTotalExpense(closure.getTotalExpense());
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
                entity.getOpeningBalance(),
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
                entity.getTotalIncome(),
                entity.getTotalExpense(),
                entity.getClosingNotes(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                details
        );
    }

    private CashMovementJpaEntity toMovementEntity(CashMovement movement) {
        var entity = new CashMovementJpaEntity();
        entity.setId(movement.getId());
        entity.setRegisterId(movement.getRegisterId().value());
        entity.setDirection(movement.getDirection().name());
        entity.setType(movement.getType().name());
        entity.setAmount(movement.getAmount());
        entity.setPaymentMethodId(movement.getPaymentMethodId());
        entity.setPaymentMethodName(movement.getPaymentMethodName());
        entity.setDescription(movement.getDescription());
        entity.setSourceType(movement.getSourceType());
        entity.setSourceId(movement.getSourceId());
        entity.setCreatedBy(movement.getCreatedBy());
        entity.setCreatedByName(movement.getCreatedByName());
        entity.setOccurredAt(movement.getOccurredAt());
        return entity;
    }

    private CashMovement toMovementDomain(CashMovementJpaEntity entity) {
        return CashMovement.reconstitute(entity.getId(), RegisterId.of(entity.getRegisterId()),
                CashMovementDirection.valueOf(entity.getDirection()), CashMovementType.valueOf(entity.getType()),
                entity.getAmount(), entity.getPaymentMethodId(), entity.getPaymentMethodName(),
                entity.getDescription(), entity.getSourceType(), entity.getSourceId(), entity.getCreatedBy(),
                entity.getCreatedByName(), entity.getOccurredAt());
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

    private BigDecimal queryDecimalRange(String sql, LocalDate dateFrom, LocalDate dateTo) {
        var result = entityManager.createNativeQuery(sql)
                .setParameter("dateFrom", dateFrom)
                .setParameter("dateTo", dateTo)
                .getSingleResult();
        return result != null ? BigDecimal.valueOf(((Number) result).doubleValue()) : BigDecimal.ZERO;
    }

    private int queryIntRange(String sql, LocalDate dateFrom, LocalDate dateTo) {
        var query = entityManager.createNativeQuery(sql);
        query.setParameter("dateFrom", dateFrom);
        query.setParameter("dateTo", dateTo);
        var result = query.getSingleResult();
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
