package com.cashly.cashly_api.sales.invoices.credit_notes.infrastructure.persistence.adapters;

import com.cashly.cashly_api.sales.invoices.credit_notes.infrastructure.persistence.repositories.CreditNoteJpaRepository;
import com.cashly.cashly_api.sales.invoices.credit_notes.infrastructure.persistence.repositories.CreditNoteNumberSequenceJpaRepository;

import com.cashly.cashly_api.sales.invoices.credit_notes.domain.entity.CreditNote;
import com.cashly.cashly_api.sales.invoices.credit_notes.domain.repository.CreditNoteRepository;
import com.cashly.cashly_api.sales.invoices.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.sales.invoices.credit_notes.infrastructure.persistence.entities.CreditNoteEntity;
import com.cashly.cashly_api.sales.invoices.credit_notes.infrastructure.persistence.entities.CreditNoteNumberSequenceEntity;
import com.cashly.cashly_api.sales.invoices.credit_notes.infrastructure.persistence.mappers.CreditNoteJpaMapper;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CreditNoteRepositoryAdapter implements CreditNoteRepository {

    private final CreditNoteJpaRepository jpaRepository;
    private final CreditNoteNumberSequenceJpaRepository sequenceJpaRepository;

    public CreditNoteRepositoryAdapter(CreditNoteJpaRepository jpaRepository,
            CreditNoteNumberSequenceJpaRepository sequenceJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.sequenceJpaRepository = sequenceJpaRepository;
    }

    @Override
    public CreditNote save(CreditNote creditNote) {
        var entity = CreditNoteJpaMapper.toEntity(creditNote);
        var saved = jpaRepository.save(entity);
        return CreditNoteJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<CreditNote> findById(CreditNoteId id) {
        return jpaRepository.findById(id.value())
                .map(CreditNoteJpaMapper::toDomain);
    }

    @Override
    public List<CreditNote> findAllWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<CreditNoteEntity> entities = jpaRepository.findAllWithFilters(from,
                to, customerId);

        return entities.stream().map(CreditNoteJpaMapper::toDomain).toList();
    }

    @Override
    public PageResponse<CreditNote> findPageWithFilters(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String search, int page,
            int size, String orderBy, String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        Page<CreditNoteEntity> result = jpaRepository.findPageWithFilters(from,
                to, customerId, searchPattern(search), PageRequest.of(page,
                        size, sort(orderBy, orderDir)));
        List<CreditNote> content = result.getContent().stream()
                .map(CreditNoteJpaMapper::toDomain).toList();
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
            case "noteNumber" -> "noteNumber";
            case "creditNoteNumber" -> "noteNumber";
            case "amount" -> "amount";
            case "createdAt" -> "createdAt";
            default -> "createdAt";
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(orderDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property).and(Sort.by(direction, "id"));
    }

    @Override
    public List<CreditNote> findByInvoiceId(UUID invoiceId) {
        List<CreditNoteEntity> entities = jpaRepository
                .findByInvoiceId(invoiceId);
        return entities.stream().map(CreditNoteJpaMapper::toDomain).toList();
    }

    @Override
    public Map<UUID, BigDecimal> sumByInvoiceIds(List<UUID> invoiceIds) {
        if (invoiceIds.isEmpty())
            return Map.of();
        List<Object[]> results = jpaRepository.sumByInvoiceIds(invoiceIds);
        return results.stream().collect(Collectors.toMap(row -> (UUID) row[0],
                row -> (BigDecimal) row[1]));
    }

    @Override
    public void delete(CreditNoteId id) {
        jpaRepository.deleteById(id.value());
    }

    @Override
    public int getNextSequenceForYear(int year) {
        CreditNoteNumberSequenceEntity sequence = sequenceJpaRepository
                .findByYearForUpdate(year).orElseGet(() -> {
                    CreditNoteNumberSequenceEntity newSeq = new CreditNoteNumberSequenceEntity();
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
