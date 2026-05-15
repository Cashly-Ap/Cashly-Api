package com.parsun.parsun_api.quotes.infrastructure.persistence;

import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
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
    public Optional<Quote> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Quote> findAll() {
        return jpaRepository.findAll().stream()
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
