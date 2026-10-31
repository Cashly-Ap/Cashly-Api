package com.cashly.cashly_api.sales.quotes.infrastructure.persistence.adapters;

import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.mappers.QuoteJpaMapper;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.repositories.QuoteJpaRepository;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteNumberSequenceJpaEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.repositories.QuoteNumberSequenceJpaRepository;

import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.entity.QuoteLine;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteLineId;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteNumber;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JpaQuoteRepositoryAdapter Unit Tests")
class JpaQuoteRepositoryAdapterTest {

    private QuoteJpaRepository jpaRepository;
    private QuoteNumberSequenceJpaRepository sequenceJpaRepository;
    private QuoteJpaMapper mapper;
    private ArgumentCaptor<QuoteJpaEntity> entityCaptor;
    private JpaQuoteRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(QuoteJpaRepository.class);
        sequenceJpaRepository = mock(QuoteNumberSequenceJpaRepository.class);
        mapper = mock(QuoteJpaMapper.class);
        entityCaptor = ArgumentCaptor.forClass(QuoteJpaEntity.class);
        adapter = new JpaQuoteRepositoryAdapter(jpaRepository,
                sequenceJpaRepository, mapper);
    }

    private QuoteLine createTestLine() {
        return QuoteLine.reconstitute(QuoteLineId.of(UUID.randomUUID()),
                UUID.randomUUID(), "Test Product", "PRD-001",
                new BigDecimal("2"), new BigDecimal("50.00"),
                new BigDecimal("100.00"), List.of());
    }

    private Quote createTestQuote() {
        return Quote.reconstitute(UUID.randomUUID(), QuoteNumber.of(2026, 1),
                UUID.randomUUID(), "Test Customer", "J-12345678-9",
                UUID.randomUUID(), "John Doe", QuoteStatus.DRAFT,
                LocalDate.now().plusDays(30), "Test notes",
                new BigDecimal("100.00"), null, null, null, null, null, null,
                List.of(createTestLine()), Instant.now(), Instant.now());
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_MapToEntityAndDelegateToJpaRepository() {
            Quote quote = createTestQuote();
            QuoteJpaEntity jpaEntity = new QuoteJpaEntity();
            jpaEntity.setId(quote.getId());
            jpaEntity.setQuoteNumber(quote.getQuoteNumber().value());
            jpaEntity.setCustomerId(quote.getCustomerId());

            when(mapper.toJpa(quote)).thenReturn(jpaEntity);
            when(jpaRepository.save(entityCaptor.capture()))
                    .thenReturn(jpaEntity);
            when(mapper.toDomain(jpaEntity)).thenReturn(quote);

            Quote saved = adapter.save(quote);

            assertNotNull(saved);
            assertEquals(quote.getId(), saved.getId());
            assertEquals(quote.getQuoteNumber(), saved.getQuoteNumber());

            QuoteJpaEntity captured = entityCaptor.getValue();
            assertEquals(quote.getId(), captured.getId());
            assertEquals(quote.getQuoteNumber().value(),
                    captured.getQuoteNumber());
            assertEquals(quote.getCustomerId(), captured.getCustomerId());

            verify(mapper).toJpa(quote);
            verify(jpaRepository).save(any(QuoteJpaEntity.class));
            verify(mapper).toDomain(jpaEntity);
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            Quote quote = createTestQuote();
            QuoteJpaEntity jpaEntity = new QuoteJpaEntity();
            jpaEntity.setId(quote.getId());

            when(jpaRepository.findByIdWithLines(quote.getId()))
                    .thenReturn(Optional.of(jpaEntity));
            when(mapper.toDomain(jpaEntity)).thenReturn(quote);

            Optional<Quote> result = adapter.findById(quote.getId());

            assertTrue(result.isPresent());
            assertEquals(quote.getId(), result.get().getId());
            assertEquals(quote.getQuoteNumber(), result.get().getQuoteNumber());
            verify(jpaRepository).findByIdWithLines(quote.getId());
            verify(mapper).toDomain(jpaEntity);
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            UUID id = UUID.randomUUID();
            when(jpaRepository.findByIdWithLines(id))
                    .thenReturn(Optional.empty());

            Optional<Quote> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(jpaRepository).findByIdWithLines(id);
            verify(mapper, never()).toDomain(any());
        }
    }

    @Nested
    @DisplayName("FindAll")
    class FindAll {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            Quote quote = createTestQuote();
            QuoteJpaEntity jpaEntity = new QuoteJpaEntity();
            jpaEntity.setId(quote.getId());

            when(jpaRepository.findAllWithLines())
                    .thenReturn(List.of(jpaEntity));
            when(mapper.toDomain(jpaEntity)).thenReturn(quote);

            List<Quote> result = adapter.findAll();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(quote.getId(), result.getFirst().getId());
            verify(jpaRepository).findAllWithLines();
            verify(mapper).toDomain(jpaEntity);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            when(jpaRepository.findAllWithLines()).thenReturn(List.of());

            List<Quote> result = adapter.findAll();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository).findAllWithLines();
            verify(mapper, never()).toDomain(any());
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            Quote quote = createTestQuote();
            QuoteJpaEntity jpaEntity = new QuoteJpaEntity();
            jpaEntity.setId(quote.getId());

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(),
                    any(), any())).thenReturn(List.of(jpaEntity));
            when(mapper.toDomain(jpaEntity)).thenReturn(quote);

            List<Quote> result = adapter.findAllWithFilters(null, null, null,
                    null, null, null, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(quote.getId(), result.getFirst().getId());
            verify(jpaRepository).findAllWithFilters(null, null, null, null,
                    null, null);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(),
                    any(), any())).thenReturn(List.of());

            List<Quote> result = adapter.findAllWithFilters(null, null, null,
                    null, null, null, null, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository).findAllWithFilters(null, null, null, null,
                    null, null);
        }

        @Test
        void should_ApplySorting_When_OrderByIsProvided() {
            Quote quote1 = Quote.reconstitute(UUID.randomUUID(),
                    QuoteNumber.of(2026, 1), UUID.randomUUID(), "A Customer",
                    "J-11111111-1", UUID.randomUUID(), "John Doe",
                    QuoteStatus.DRAFT, LocalDate.now().plusDays(30), "Notes",
                    new BigDecimal("100.00"), null, null, null, null, null,
                    null, List.of(createTestLine()), Instant.now(),
                    Instant.now());

            Quote quote2 = Quote.reconstitute(UUID.randomUUID(),
                    QuoteNumber.of(2026, 2), UUID.randomUUID(), "B Customer",
                    "J-22222222-2", UUID.randomUUID(), "Jane Doe",
                    QuoteStatus.DRAFT, LocalDate.now().plusDays(15), "Notes",
                    new BigDecimal("200.00"), null, null, null, null, null,
                    null, List.of(createTestLine()), Instant.now(),
                    Instant.now());

            QuoteJpaEntity entity1 = new QuoteJpaEntity();
            entity1.setId(quote1.getId());
            entity1.setQuoteNumber(quote1.getQuoteNumber().value());
            entity1.setCustomerName(quote1.getCustomerName());
            entity1.setSubtotal(quote1.getSubtotal());

            QuoteJpaEntity entity2 = new QuoteJpaEntity();
            entity2.setId(quote2.getId());
            entity2.setQuoteNumber(quote2.getQuoteNumber().value());
            entity2.setCustomerName(quote2.getCustomerName());
            entity2.setSubtotal(quote2.getSubtotal());

            when(jpaRepository.findAllWithFilters(any(), any(), any(), any(),
                    any(), any())).thenReturn(List.of(entity2, entity1));
            when(mapper.toDomain(entity1)).thenReturn(quote1);
            when(mapper.toDomain(entity2)).thenReturn(quote2);

            List<Quote> result = adapter.findAllWithFilters(null, null, null,
                    null, null, null, "customerName", "asc");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("A Customer", result.getFirst().getCustomerName());
            assertEquals("B Customer", result.getLast().getCustomerName());
        }
    }

    @Nested
    @DisplayName("ExistsByQuoteNumber")
    class ExistsByQuoteNumber {

        @Test
        void should_ReturnTrue_When_QuoteNumberExists() {
            QuoteNumber quoteNumber = QuoteNumber.of(2026, 1);
            when(jpaRepository.existsByQuoteNumber(quoteNumber.value()))
                    .thenReturn(true);

            boolean exists = adapter.existsByQuoteNumber(quoteNumber);

            assertTrue(exists);
            verify(jpaRepository).existsByQuoteNumber(quoteNumber.value());
        }

        @Test
        void should_ReturnFalse_When_QuoteNumberDoesNotExist() {
            QuoteNumber quoteNumber = QuoteNumber.of(2026, 9999);
            when(jpaRepository.existsByQuoteNumber(quoteNumber.value()))
                    .thenReturn(false);

            boolean exists = adapter.existsByQuoteNumber(quoteNumber);

            assertFalse(exists);
            verify(jpaRepository).existsByQuoteNumber(quoteNumber.value());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        void should_DelegateToJpaRepository() {
            UUID id = UUID.randomUUID();

            adapter.delete(id);

            verify(jpaRepository).deleteById(id);
        }
    }

    @Nested
    @DisplayName("GetNextSequenceForYear")
    class GetNextSequenceForYear {

        @Test
        void should_ReturnNextValueAndSave_When_ExistingSequenceFound() {
            int year = LocalDate.now().getYear();
            QuoteNumberSequenceJpaEntity existingSeq = new QuoteNumberSequenceJpaEntity();
            existingSeq.setYear(year);
            existingSeq.setNextValue(5);

            when(sequenceJpaRepository.findByYearForUpdate(year))
                    .thenReturn(Optional.of(existingSeq));

            int next = adapter.getNextSequenceForYear(year);

            assertEquals(6, next);
            assertEquals(6, existingSeq.getNextValue().intValue());
            verify(sequenceJpaRepository).findByYearForUpdate(year);
            verify(sequenceJpaRepository).save(existingSeq);
        }

        @Test
        void should_CreateNewSequenceAndReturnOne_When_NoExistingSequence() {
            int year = LocalDate.now().getYear();

            when(sequenceJpaRepository.findByYearForUpdate(year))
                    .thenReturn(Optional.empty());

            int next = adapter.getNextSequenceForYear(year);

            assertEquals(1, next);
            verify(sequenceJpaRepository).findByYearForUpdate(year);
            verify(sequenceJpaRepository)
                    .save(any(QuoteNumberSequenceJpaEntity.class));
        }
    }
}
