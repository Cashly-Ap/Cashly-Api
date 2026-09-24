package com.parsun.parsun_api.invoices.infrastructure.persistence.repositories;

import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceNumberSequenceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.mappers.InvoiceJpaMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        var saved = jpaRepository.save(entity);
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
                .map(InvoiceJpaMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(InvoiceId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public List<Invoice> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                             UUID customerId, List<String> statuses,
                                             List<String> returnStatus,
                                             String dueStatus,
                                             UUID productId, UUID categoryId,
                                             String orderBy, String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<InvoiceEntity> entities = jpaRepository.findAllWithFilters(from, to, customerId, statuses, returnStatus, dueStatus, productId, categoryId);

        return sortAndMap(entities, orderBy, orderDir);
    }

    public List<Invoice> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                             UUID customerId, List<String> statuses,
                                             List<String> returnStatus,
                                             UUID productId, UUID categoryId,
                                             String orderBy, String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        List<InvoiceEntity> entities = jpaRepository.findAllWithFilters(
                from, to, customerId, statuses, returnStatus, productId, categoryId);
        return sortAndMap(entities, orderBy, orderDir);
    }

    private List<Invoice> sortAndMap(List<InvoiceEntity> entities, String orderBy, String orderDir) {
        boolean desc = "desc".equalsIgnoreCase(orderDir);
        Comparator<InvoiceEntity> comparator = switch (orderBy) {
            case null -> Comparator.comparing(InvoiceEntity::getCreatedAt);
            case "invoiceNumber" -> Comparator.comparing(InvoiceEntity::getInvoiceNumber);
            case "customerName" -> Comparator.comparing(InvoiceEntity::getCustomerName);
            case "status" -> Comparator.comparing(InvoiceEntity::getStatus);
            case "subtotal" -> Comparator.comparing(InvoiceEntity::getSubtotal);
            case "total" -> Comparator.comparing((InvoiceEntity e) -> e.getSubtotal().subtract(e.getReturnedTotal() != null ? e.getReturnedTotal() : java.math.BigDecimal.ZERO));
            case "paymentType" -> Comparator.comparing(InvoiceEntity::getPaymentType);
            default -> Comparator.comparing(InvoiceEntity::getCreatedAt);
        };

        if (desc) {
            comparator = comparator.reversed();
        }

        return entities.stream()
                .sorted(comparator)
                .map(InvoiceJpaMapper::toDomain)
                .toList();
    }

    @Override
    public int getNextSequenceForYear(int year) {
        InvoiceNumberSequenceEntity sequence = sequenceJpaRepository.findByYearForUpdate(year)
                .orElseGet(() -> {
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
