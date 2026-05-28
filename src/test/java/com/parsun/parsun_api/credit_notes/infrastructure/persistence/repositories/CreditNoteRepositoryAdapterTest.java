package com.parsun.parsun_api.credit_notes.infrastructure.persistence.repositories;

import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import com.parsun.parsun_api.credit_notes.infrastructure.persistence.entities.CreditNoteEntity;
import com.parsun.parsun_api.credit_notes.infrastructure.persistence.entities.CreditNoteNumberSequenceEntity;
import com.parsun.parsun_api.credit_notes.infrastructure.persistence.mappers.CreditNoteJpaMapper;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditNoteRepositoryAdapter Unit Tests")
class CreditNoteRepositoryAdapterTest {

    @Mock
    private CreditNoteJpaRepository jpaRepository;

    @Mock
    private CreditNoteNumberSequenceJpaRepository sequenceJpaRepository;

    @Captor
    private ArgumentCaptor<CreditNoteEntity> entityCaptor;

    private CreditNoteRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CreditNoteRepositoryAdapter(jpaRepository, sequenceJpaRepository);
    }

    @Nested
    @DisplayName("Save")
    class Save {

        @Test
        void should_MapToEntityAndDelegateToJpaRepository() {
            CreditNote creditNote = CreditNote.reconstitute(
                    CreditNoteId.of(UUID.randomUUID()),
                    CreditNoteNumber.of(2026, 1),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new BigDecimal("150.00"),
                    "Product damaged",
                    UUID.randomUUID(),
                    "John Doe",
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );

            CreditNoteEntity expectedEntity = CreditNoteJpaMapper.toEntity(creditNote);
            when(jpaRepository.save(entityCaptor.capture())).thenReturn(expectedEntity);

            CreditNote saved = adapter.save(creditNote);

            assertNotNull(saved);
            assertEquals(creditNote.getId(), saved.getId());
            assertEquals(creditNote.getNoteNumber(), saved.getNoteNumber());
            assertEquals(creditNote.getAmount(), saved.getAmount());
            assertEquals(creditNote.getReason(), saved.getReason());

            CreditNoteEntity captured = entityCaptor.getValue();
            assertEquals(creditNote.getId().value(), captured.getId());
            assertEquals(creditNote.getNoteNumber().value(), captured.getNoteNumber());
            assertEquals(creditNote.getCustomerId(), captured.getCustomerId());
            assertEquals(creditNote.getInvoiceId(), captured.getInvoiceId());
            assertEquals(creditNote.getAmount(), captured.getAmount());
            assertEquals(creditNote.getReason(), captured.getReason());
            assertEquals(creditNote.getCreatedBy(), captured.getCreatedBy());
            assertEquals(creditNote.getCreatedByName(), captured.getCreatedByName());
            assertEquals(creditNote.getCreatedAt(), captured.getCreatedAt());
            assertEquals(creditNote.getUpdatedAt(), captured.getUpdatedAt());

            verify(jpaRepository).save(any(CreditNoteEntity.class));
        }
    }

    @Nested
    @DisplayName("FindById")
    class FindById {

        @Test
        void should_ReturnDomainObject_When_EntityFound() {
            CreditNoteId id = CreditNoteId.generate();
            CreditNoteEntity entity = CreditNoteJpaMapper.toEntity(
                    CreditNote.reconstitute(
                            id,
                            CreditNoteNumber.of(2026, 1),
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

            Optional<CreditNote> result = adapter.findById(id);

            assertTrue(result.isPresent());
            assertEquals(id, result.get().getId());
            verify(jpaRepository).findById(id.value());
        }

        @Test
        void should_ReturnEmpty_When_EntityNotFound() {
            CreditNoteId id = CreditNoteId.generate();
            when(jpaRepository.findById(id.value())).thenReturn(Optional.empty());

            Optional<CreditNote> result = adapter.findById(id);

            assertTrue(result.isEmpty());
            verify(jpaRepository).findById(id.value());
        }
    }

    @Nested
    @DisplayName("FindAllWithFilters")
    class FindAllWithFilters {

        @Test
        void should_ReturnDomainObjects_When_EntitiesFound() {
            CreditNoteEntity entity = CreditNoteJpaMapper.toEntity(
                    CreditNote.reconstitute(
                            CreditNoteId.generate(),
                            CreditNoteNumber.of(2026, 1),
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

            when(jpaRepository.findAllWithFilters(any(), any(), any())).thenReturn(List.of(entity));

            List<CreditNote> result = adapter.findAllWithFilters(null, null, null);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(jpaRepository).findAllWithFilters(null, null, null);
        }

        @Test
        void should_ReturnEmptyList_When_NoEntitiesFound() {
            when(jpaRepository.findAllWithFilters(any(), any(), any())).thenReturn(List.of());

            List<CreditNote> result = adapter.findAllWithFilters(null, null, null);

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
            CreditNoteEntity entity = CreditNoteJpaMapper.toEntity(
                    CreditNote.reconstitute(
                            CreditNoteId.generate(),
                            CreditNoteNumber.of(2026, 1),
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

            List<CreditNote> result = adapter.findByInvoiceId(invoiceId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(invoiceId, result.get(0).getInvoiceId());
            verify(jpaRepository).findByInvoiceId(invoiceId);
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {

        @Test
        void should_DelegateToJpaRepository() {
            CreditNoteId id = CreditNoteId.generate();

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
            CreditNoteNumberSequenceEntity existingSeq = new CreditNoteNumberSequenceEntity();
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
            verify(sequenceJpaRepository).save(any(CreditNoteNumberSequenceEntity.class));
        }
    }
}
