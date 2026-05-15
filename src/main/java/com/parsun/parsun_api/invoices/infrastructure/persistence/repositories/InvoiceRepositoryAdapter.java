package com.parsun.parsun_api.invoices.infrastructure.persistence.repositories;

import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceNumberSequenceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.mappers.InvoiceJpaMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private static final Set<String> ALLOWED_ORDER_BY = Set.of(
        "createdAt", "invoiceNumber", "customerName", "status", "subtotal", "paymentType"
    );

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
    public void deleteById(InvoiceId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public List<Invoice> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                             UUID customerId, String status,
                                             String orderBy, String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        String field = ALLOWED_ORDER_BY.contains(orderBy) ? orderBy : "createdAt";
        Sort sort = "desc".equalsIgnoreCase(orderDir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        return jpaRepository.findAllWithFilters(from, to, customerId, status, sort)
                .stream()
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
