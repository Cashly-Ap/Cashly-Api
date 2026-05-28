package com.parsun.parsun_api.debit_notes.infrastructure.persistence.repositories;

import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import com.parsun.parsun_api.debit_notes.infrastructure.persistence.entities.DebitNoteEntity;
import com.parsun.parsun_api.debit_notes.infrastructure.persistence.entities.DebitNoteNumberSequenceEntity;
import com.parsun.parsun_api.debit_notes.infrastructure.persistence.mappers.DebitNoteJpaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private DebitNoteRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DebitNoteRepositoryAdapter(jpaRepository, sequenceJpaRepository);
    }

    @Test
    void should_SaveDebitNote_When_ValidDebitNoteProvided() {
        DebitNoteNumber noteNumber = DebitNoteNumber.of(2026, 1);
        DebitNote debitNote = DebitNote.create(
                noteNumber,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(1500),
                "Defect",
                UUID.randomUUID(),
                "User"
        );

        DebitNoteEntity expectedEntity = DebitNoteJpaMapper.toEntity(debitNote);
        when(jpaRepository.save(any(DebitNoteEntity.class))).thenReturn(expectedEntity);

        DebitNote saved = adapter.save(debitNote);

        assertNotNull(saved);
        assertEquals(debitNote.getId(), saved.getId());
        assertEquals(debitNote.getNoteNumber(), saved.getNoteNumber());
        assertEquals(debitNote.getAmount(), saved.getAmount());

        ArgumentCaptor<DebitNoteEntity> captor = ArgumentCaptor.forClass(DebitNoteEntity.class);
        verify(jpaRepository).save(captor.capture());

        DebitNoteEntity captured = captor.getValue();
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
    }
}
