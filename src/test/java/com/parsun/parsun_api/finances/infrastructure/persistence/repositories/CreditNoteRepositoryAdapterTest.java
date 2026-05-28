package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.domain.entities.CreditNote;
import com.parsun.parsun_api.finances.infrastructure.persistence.entities.CreditNoteEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditNoteRepositoryAdapter Tests")
class CreditNoteRepositoryAdapterTest {

    @Mock
    private CreditNoteJpaRepository jpaRepository;

    @Mock
    private CreditNoteNumberSequenceJpaRepository sequenceJpaRepository;

    @InjectMocks
    private CreditNoteRepositoryAdapter adapter;

    @Test
    void findByInvoiceId_ShouldReturnAllNotes() {
        UUID invoiceId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();

        CreditNoteEntity appliedEntity = new CreditNoteEntity();
        appliedEntity.setId(noteId);
        appliedEntity.setNoteNumber("NC-2026-00001");
        appliedEntity.setCustomerId(UUID.randomUUID());
        appliedEntity.setInvoiceId(invoiceId);
        appliedEntity.setAmount(BigDecimal.valueOf(100));
        appliedEntity.setReason("Test credit");
        appliedEntity.setCreatedBy(UUID.randomUUID());
        appliedEntity.setCreatedByName("Test User");
        appliedEntity.setCreatedAt(java.time.LocalDateTime.now());
        appliedEntity.setUpdatedAt(java.time.LocalDateTime.now());

        when(jpaRepository.findByInvoiceId(invoiceId))
                .thenReturn(List.of(appliedEntity));

        List<CreditNote> result = adapter.findByInvoiceId(invoiceId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId().value()).isEqualTo(noteId);
        verify(jpaRepository).findByInvoiceId(invoiceId);
        verifyNoMoreInteractions(jpaRepository);
    }

    @Test
    void findByInvoiceId_ShouldReturnEmptyList_WhenNoNotes() {
        UUID invoiceId = UUID.randomUUID();

        when(jpaRepository.findByInvoiceId(invoiceId))
                .thenReturn(List.of());

        List<CreditNote> result = adapter.findByInvoiceId(invoiceId);

        assertThat(result).isEmpty();
        verify(jpaRepository).findByInvoiceId(invoiceId);
    }
}
