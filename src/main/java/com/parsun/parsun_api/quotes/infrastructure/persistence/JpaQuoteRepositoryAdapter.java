package com.parsun.parsun_api.quotes.infrastructure.persistence;

import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaQuoteRepositoryAdapter implements QuoteRepository {

    private final QuoteJpaRepository jpaRepository;
    private final QuoteNumberSequenceJpaRepository sequenceJpaRepository;
    private final QuoteJpaMapper mapper;

    public JpaQuoteRepositoryAdapter(QuoteJpaRepository jpaRepository,
                                      QuoteNumberSequenceJpaRepository sequenceJpaRepository,
                                      QuoteJpaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.sequenceJpaRepository = sequenceJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Quote save(Quote quote) {
        QuoteJpaEntity entity = mapper.toJpa(quote);
        QuoteJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quote> findById(UUID id) {
        return jpaRepository.findByIdWithLines(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quote> findAll() {
        return jpaRepository.findAllWithLines().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Quote> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                           UUID customerId, String status,
                                           UUID productId, UUID categoryId,
                                           String orderBy, String orderDir) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<QuoteJpaEntity> entities = jpaRepository.findAllWithFilters(from, to, customerId, status, productId, categoryId);

        boolean desc = "desc".equalsIgnoreCase(orderDir);
        Comparator<QuoteJpaEntity> comparator = switch (orderBy) {
            case "quoteNumber" -> Comparator.comparing(QuoteJpaEntity::getQuoteNumber);
            case "customerName" -> Comparator.comparing(QuoteJpaEntity::getCustomerName);
            case "status" -> Comparator.comparing(QuoteJpaEntity::getStatus);
            case "subtotal" -> Comparator.comparing(QuoteJpaEntity::getSubtotal);
            default -> Comparator.comparing(QuoteJpaEntity::getCreatedAt);
        };

        if (desc) {
            comparator = comparator.reversed();
        }

        return entities.stream()
                .sorted(comparator)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByQuoteNumber(QuoteNumber quoteNumber) {
        return jpaRepository.existsByQuoteNumber(quoteNumber.value());
    }

    @Override
    @Transactional
    public int getNextSequenceForYear(int year) {
        QuoteNumberSequenceJpaEntity sequence = sequenceJpaRepository.findByYearForUpdate(year)
                .orElseGet(() -> {
                    QuoteNumberSequenceJpaEntity newSeq = new QuoteNumberSequenceJpaEntity();
                    newSeq.setYear(year);
                    newSeq.setNextValue(0);
                    return newSeq;
                });

        int next = sequence.getNextValue() + 1;
        sequence.setNextValue(next);
        sequenceJpaRepository.save(sequence);
        return next;
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
