package com.parsun.parsun_api.debit_notes.infrastructure.persistence.repositories;

import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import com.parsun.parsun_api.debit_notes.infrastructure.persistence.entities.DebitNoteEntity;
import com.parsun.parsun_api.debit_notes.infrastructure.persistence.entities.DebitNoteNumberSequenceEntity;
import com.parsun.parsun_api.debit_notes.infrastructure.persistence.mappers.DebitNoteJpaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DebitNoteRepositoryAdapter Unit Tests")
class DebitNoteRepositoryAdapterTest {

    @Mock
    private DebitNoteJpaRepository jpaRepository;

    @Mock
    private DebitNoteNumberSequenceJpaRepository sequenceJpaRepository;

    @Captor
    private ArgumentCaptor<DebitNoteEntity> entityCaptor;

    private DebitNoteRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DebitNoteRepositoryAdapter(jpaRepository, sequenceJpaRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_MapToEntityAndDelegateToJpaRepository() {
            DebitNote debitNote = DebitNote.reconstitute(
                    DebitNoteId.of(UUID.randomUUID()),
                    DebitNoteNumber.of(2026, 1),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new BigDecimal("150.00"),
                    "Product damaged",
                    UUID.randomUUID(),
                    "John Doe",
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            DebitNoteEntity expectedEntity = DebitNoteJpaMapper.toEntity(debitNote);
            when(jpaRepository.save(entityCaptor.capture())).thenReturn(expectedEntity);

            DebitNote saved = adapter.save(debitNote);

            assertNotNull(saved);
            assertEquals(debitNote.getId(), saved.getId());
            assertEquals(debitNote.getNoteNumber(), saved.getNoteNumber());
            assertEquals(debitNote.getAmount(), saved.getAmount());
            assertEquals(debitNote.getReason(), saved.getReason());

            DebitNoteEntity captured = entityCaptor.getValue();
            assertEquals(debitNote.getId().value(), captured.getId());
            assertEquals(debitNote.getNoteNumber().value(), captured.getNoteNumber());
            assertEquals(debitNote.getCustomerId(), captured.getCustomerId());
            assertEquals(debitNote.getInvoiceId(), captured.getInvoiceId());
            assertEquals(debitNote.getAmount(), captured.getAmount());
            assertEquals(debitNote.getReason(), captured.getReason());
            assertEquals(debitNote.getCreatedBy(), captured.getCreatedBy());
            assertEquals(debitNote.getCreatedByName(), captured.getCreatedByName());
            assertEquals(debitNote.getCreatedAt(), captured.getCreatedAt());
            assertEquals(debitNote.getUpdatedAt(), captured.getUpdatedAt());

            verify(jpaRepository).save(any(DebitNoteEntity.class));
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            DebitNoteId id = DebitNoteId.generate();
            DebitNoteEntity entity = DebitNoteJpaMapper.toEntity(
                    DebitNote.reconstitute(
                            id,
                            DebitNoteNumber.of(2026, 1),
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            new BigDecimal("150.00"),
                            "Product damaged",
                            UUID.randomUUID(),
                            "John Doe",
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    )
            );

            when(jpaRepository.findById(id.value())).thenReturn(Optional.of(entity));

            Optional<DebitNote> result = adapter.findById(id);

            assertTrue(result.isPresent());
            assertEquals(id, result.get().getId());
            verify(jpaRepository).findById(id.value());
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            DebitNoteId id = DebitNoteId.generate();
            when(jpaRepository.findById(id.value())).thenReturn(Optional.empty());

            Optional<DebitNote> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(jpaRepository).findById(id.value());
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @Test
        void should_ReturnDomainObjects_When_AllFiltersProvided() {
            LocalDate dateFrom = LocalDate.of(2026, 1, 1);
            LocalDate dateTo = LocalDate.of(2026, 12, 31);
            UUID customerId = UUID.randomUUID();

            DebitNoteEntity entity = DebitNoteJpaMapper.toEntity(
                    DebitNote.reconstitute(
                            DebitNoteId.generate(),
                            DebitNoteNumber.of(2026, 1),
                            customerId,
                            UUID.randomUUID(),
                            new BigDecimal("150.00"),
                            "Product damaged",
                            UUID.randomUUID(),
                            "John Doe",
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    )
            );

            when(jpaRepository.findAllWithFilters(any(), any(), any())).thenReturn(List.of(entity));

            List<DebitNote> result = adapter.findAllWithFilters(dateFrom, dateTo, customerId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(customerId, result.get(0).getCustomerId());

            LocalDateTime expectedFrom = dateFrom.atStartOfDay();
            LocalDateTime expectedTo = dateTo.atTime(23, 59, 59);
            verify(jpaRepository).findAllWithFilters(expectedFrom, expectedTo, customerId);
        }

        @Test
        void should_ReturnDomainObjects_When_PartialFiltersProvided() {
            UUID customerId = UUID.randomUUID();

            DebitNoteEntity entity = DebitNoteJpaMapper.toEntity(
                    DebitNote.reconstitute(
                            DebitNoteId.generate(),
                            DebitNoteNumber.of(2026, 1),
                            customerId,
                            UUID.randomUUID(),
                            new BigDecimal("150.00"),
                            "Product damaged",
                            UUID.randomUUID(),
                            "John Doe",
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    )
            );

            when(jpaRepository.findAllWithFilters(any(), any(), any())).thenReturn(List.of(entity));

            List<DebitNote> result = adapter.findAllWithFilters(null, null, customerId);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(jpaRepository).findAllWithFilters(null, null, customerId);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            when(jpaRepository.findAllWithFilters(any(), any(), any())).thenReturn(List.of());

            List<DebitNote> result = adapter.findAllWithFilters(null, null, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository).findAllWithFilters(null, null, null);
        }
    }

    @Nested
    @DisplayName("FindByInvoiceId")
    class FindByInvoiceId {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            UUID invoiceId = UUID.randomUUID();
            DebitNoteEntity entity = DebitNoteJpaMapper.toEntity(
                    DebitNote.reconstitute(
                            DebitNoteId.generate(),
                            DebitNoteNumber.of(2026, 1),
                            UUID.randomUUID(),
                            invoiceId,
                            new BigDecimal("150.00"),
                            "Product damaged",
                            UUID.randomUUID(),
                            "John Doe",
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    )
            );

            when(jpaRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(entity));

            List<DebitNote> result = adapter.findByInvoiceId(invoiceId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(invoiceId, result.get(0).getInvoiceId());
            verify(jpaRepository).findByInvoiceId(invoiceId);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            UUID invoiceId = UUID.randomUUID();
            when(jpaRepository.findByInvoiceId(invoiceId)).thenReturn(List.of());

            List<DebitNote> result = adapter.findByInvoiceId(invoiceId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository).findByInvoiceId(invoiceId);
        }
    }

    @Nested
    @DisplayName("SumByInvoiceIds")
    class SumByInvoiceIds {

        @Test
        void should_ReturnSumMap_When_InvoiceIdsProvided() {
            UUID invoiceId1 = UUID.randomUUID();
            UUID invoiceId2 = UUID.randomUUID();
            List<UUID> invoiceIds = List.of(invoiceId1, invoiceId2);

            Object[] row1 = new Object[]{invoiceId1, BigDecimal.valueOf(100.00)};
            Object[] row2 = new Object[]{invoiceId2, BigDecimal.valueOf(250.50)};
            when(jpaRepository.sumByInvoiceIds(invoiceIds)).thenReturn(List.of(row1, row2));

            Map<UUID, BigDecimal> result = adapter.sumByInvoiceIds(invoiceIds);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(0, BigDecimal.valueOf(100.00).compareTo(result.get(invoiceId1)));
            assertEquals(0, BigDecimal.valueOf(250.50).compareTo(result.get(invoiceId2)));
            verify(jpaRepository).sumByInvoiceIds(invoiceIds);
        }

        @Test
        void should_ReturnEmptyMap_When_EmptyInvoiceIdsList() {
            Map<UUID, BigDecimal> result = adapter.sumByInvoiceIds(List.of());

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jpaRepository, never()).sumByInvoiceIds(any());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        void should_DelegateToJpaRepository() {
            DebitNoteId id = DebitNoteId.generate();

            adapter.delete(id);

            verify(jpaRepository).deleteById(id.value());
        }
    }

    @Nested
    @DisplayName("GetNextSequenceForYear")
    class GetNextSequenceForYear {

        @Test
        void should_ReturnNextValueAndSave_When_ExistingSequenceFound() {
            int year = LocalDate.now().getYear();
            DebitNoteNumberSequenceEntity existingSeq = new DebitNoteNumberSequenceEntity();
            existingSeq.setYear(year);
            existingSeq.setNextValue(5);

            when(sequenceJpaRepository.findByYearForUpdate(year)).thenReturn(Optional.of(existingSeq));

            int next = adapter.getNextSequenceForYear(year);

            assertEquals(6, next);
            assertEquals(6, existingSeq.getNextValue().intValue());
            verify(sequenceJpaRepository).findByYearForUpdate(year);
            verify(sequenceJpaRepository).save(existingSeq);
        }

        @Test
        void should_CreateNewSequenceAndReturnOne_When_NoExistingSequence() {
            int year = LocalDate.now().getYear();

            when(sequenceJpaRepository.findByYearForUpdate(year)).thenReturn(Optional.empty());

            int next = adapter.getNextSequenceForYear(year);

            assertEquals(1, next);
            verify(sequenceJpaRepository).findByYearForUpdate(year);
            verify(sequenceJpaRepository).save(any(DebitNoteNumberSequenceEntity.class));
        }
    }
}
