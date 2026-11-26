package com.cashly.cashly_api.sales.invoices.invoices.infrastructure.persistence.adapters;

import com.cashly.cashly_api.sales.invoices.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
import com.cashly.cashly_api.sales.invoices.invoices.infrastructure.persistence.repositories.InvoiceNumberSequenceJpaRepository;

import com.cashly.cashly_api.sales.invoices.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.sales.invoices.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.sales.invoices.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.sales.invoices.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.sales.invoices.invoices.infrastructure.persistence.entities.InvoiceNumberSequenceEntity;
import com.cashly.cashly_api.sales.invoices.invoices.infrastructure.persistence.mappers.InvoiceJpaMapper;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final InvoiceJpaRepository jpaRepository;
    private final InvoiceNumberSequenceJpaRepository sequenceJpaRepository;

    public InvoiceRepositoryAdapter(InvoiceJpaRepository jpaRepository,
            InvoiceNumberSequenceJpaRepository sequenceJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.sequenceJpaRepository = sequenceJpaRepository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        var entity = InvoiceJpaMapper.toEntity(invoice);
        // Other modules insert foreign-key allocations during the surrounding transaction.
        var saved = jpaRepository.saveAndFlush(entity);
        return InvoiceJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Invoice> findById(InvoiceId id) {
        return jpaRepository.findWithLinesById(id.value())
                .map(InvoiceJpaMapper::toDomain);
    }

    @Override
    public List<Invoice> findAllById(Collection<InvoiceId> ids) {
        List<UUID> uuidIds = ids.stream().map(InvoiceId::value).toList();
        return jpaRepository.findAllById(uuidIds).stream()
                .map(InvoiceJpaMapper::toDomain).toList();
    }

    @Override
    public void deleteById(InvoiceId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public List<Invoice> findAllWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, List<String> statuses,
            List<String> returnStatus, String dueStatus, UUID productId,
            UUID categoryId, String orderBy, String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<InvoiceEntity> entities = jpaRepository.findAllWithFilters(from,
                to, customerId, statuses, returnStatus, dueStatus, productId,
                categoryId);

        return sortAndMap(entities, orderBy, orderDir);
    }

    public List<Invoice> findAllWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, List<String> statuses,
            List<String> returnStatus, UUID productId, UUID categoryId,
            String orderBy, String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        List<InvoiceEntity> entities = jpaRepository.findAllWithFilters(from,
                to, customerId, statuses, returnStatus, productId, categoryId);
        return sortAndMap(entities, orderBy, orderDir);
    }

    @Override
    public List<ReportRow> findReportRows(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, List<String> statuses, UUID productId,
            UUID categoryId) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        var rows = jpaRepository.findReportRows(from, to, customerId,
                (statuses == null || statuses.isEmpty()) ? null : statuses,
                productId, categoryId);
        var lineCounts = new java.util.HashMap<UUID, Long>();
        if (!rows.isEmpty()) {
            var ids = rows.stream().map(row -> (UUID) row[0]).toList();
            for (Object[] count : jpaRepository.countLinesByInvoiceIds(ids)) {
                lineCounts.put((UUID) count[0],
                        ((Number) count[1]).longValue());
            }
        }
        return rows.stream()
                .map(row -> new ReportRow((UUID) row[0], (String) row[1],
                        (String) row[2], (String) row[3], (String) row[4],
                        ((java.math.BigDecimal) row[5]).subtract(
                                row[6] == null ? java.math.BigDecimal.ZERO
                                        : (java.math.BigDecimal) row[6]),
                        row[7] instanceof java.time.LocalDateTime value
                                ? value
                                : ((java.sql.Timestamp) row[7])
                                        .toLocalDateTime(),
                        lineCounts.getOrDefault((UUID) row[0], 0L)))
                .toList();
    }

    @Override
    public void updateStatuses(java.util.Collection<UUID> invoiceIds,
            String status) {
        if (invoiceIds != null && !invoiceIds.isEmpty()) {
            jpaRepository.updateStatuses(invoiceIds, status);
        }
    }

    @Override
    public List<InvoiceHeader> findHeaders(UUID customerId,
            List<String> statuses) {
        var rows = (statuses == null || statuses.isEmpty())
                ? jpaRepository.findHeadersByCustomer(customerId)
                : jpaRepository.findHeadersByCustomerAndStatusIn(customerId,
                        statuses);
        return rows.stream().map(this::header).toList();
    }

    @Override
    public List<InvoiceHeader> findAllHeaders() {
        return jpaRepository.findAllHeaders().stream().map(this::header)
                .toList();
    }

    private InvoiceHeader header(
            com.cashly.cashly_api.sales.invoices.invoices.infrastructure.persistence.projections.InvoiceHeaderProjection row) {
        return new InvoiceHeader(row.getId(), row.getCustomerId(),
                row.getInvoiceNumber(), row.getStatus(), row.getSubtotal(),
                row.getReturnedTotal(), row.getDueDate());
    }

    @Override
    public PageResponse<Invoice> findPageWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, List<String> statuses,
            String search, List<String> returnStatus, String dueStatus,
            String paymentType, UUID productId, UUID categoryId, int page,
            int size, String orderBy, String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        Page<InvoiceEntity> result = jpaRepository.findPageWithFilters(from,
                to, customerId, statuses, returnStatus, dueStatus, paymentType,
                productId, categoryId, searchPattern(search),
                PageRequest.of(page, size, sort(orderBy, orderDir)));
        if (result.isEmpty()) {
            return new PageResponse<>(List.of(), result.getTotalElements(),
                    result.getTotalPages(), result.getSize(),
                    result.getNumber(), result.isFirst(), result.isLast(),
                    true);
        }
        List<UUID> orderedIds = result.getContent().stream()
                .map(InvoiceEntity::getId).toList();
        Map<UUID, InvoiceEntity> entities = jpaRepository
                .findAllWithLinesByIds(orderedIds).stream()
                .collect(Collectors.toMap(InvoiceEntity::getId,
                        Function.identity()));
        List<Invoice> content = orderedIds.stream().map(entities::get)
                .filter(Objects::nonNull).map(InvoiceJpaMapper::toDomain)
                .toList();
        return new PageResponse<>(content, result.getTotalElements(),
                result.getTotalPages(), result.getSize(), result.getNumber(),
                result.isFirst(), result.isLast(), content.isEmpty());
    }

    private static String searchPattern(String search) {
        if (search == null || search.isBlank()) {
            return "";
        }
        return "%" + search.trim().toLowerCase() + "%";
    }

    private static Sort sort(String orderBy, String orderDir) {
        String property = switch (orderBy == null ? "" : orderBy) {
            case "invoiceNumber" -> "invoiceNumber";
            case "customerName" -> "customerName";
            case "status" -> "status";
            case "subtotal" -> "subtotal";
            case "total" -> "subtotal";
            case "paymentType" -> "paymentType";
            default -> "createdAt";
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(orderDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property).and(Sort.by(direction, "id"));
    }

    private List<Invoice> sortAndMap(List<InvoiceEntity> entities,
            String orderBy, String orderDir) {
        boolean desc = "desc".equalsIgnoreCase(orderDir);
        Comparator<InvoiceEntity> comparator = switch (orderBy) {
            case null -> Comparator.comparing(InvoiceEntity::getCreatedAt);
            case "invoiceNumber" ->
                Comparator.comparing(InvoiceEntity::getInvoiceNumber);
            case "customerName" ->
                Comparator.comparing(InvoiceEntity::getCustomerName);
            case "status" -> Comparator.comparing(InvoiceEntity::getStatus);
            case "subtotal" -> Comparator.comparing(InvoiceEntity::getSubtotal);
            case "total" ->
                Comparator.comparing((InvoiceEntity e) -> e.getSubtotal()
                        .subtract(e.getReturnedTotal() != null
                                ? e.getReturnedTotal()
                                : java.math.BigDecimal.ZERO));
            case "paymentType" ->
                Comparator.comparing(InvoiceEntity::getPaymentType);
            default -> Comparator.comparing(InvoiceEntity::getCreatedAt);
        };

        if (desc) {
            comparator = comparator.reversed();
        }

        return entities.stream().sorted(comparator)
                .map(InvoiceJpaMapper::toDomain).toList();
    }

    @Override
    public int getNextSequenceForYear(int year) {
        InvoiceNumberSequenceEntity sequence = sequenceJpaRepository
                .findByYearForUpdate(year).orElseGet(() -> {
                    InvoiceNumberSequenceEntity newSeq = new InvoiceNumberSequenceEntity();
                    newSeq.setYear(year);
                    newSeq.setNextValue(0);
                    return newSeq;
                });

        int next = sequence.getNextValue() + 1;
        sequence.setNextValue(next);
        sequenceJpaRepository.save(sequence);
        return next;
    }
}
