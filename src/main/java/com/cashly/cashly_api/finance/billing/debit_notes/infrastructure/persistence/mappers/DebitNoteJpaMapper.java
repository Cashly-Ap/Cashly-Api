package com.cashly.cashly_api.finance.billing.debit_notes.infrastructure.persistence.mappers;

import com.cashly.cashly_api.finance.billing.debit_notes.domain.entity.DebitNote;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.valueobjects.DebitNoteNumber;
import com.cashly.cashly_api.finance.billing.debit_notes.infrastructure.persistence.entities.DebitNoteEntity;

public class DebitNoteJpaMapper {

    private DebitNoteJpaMapper() {}

    public static DebitNoteEntity toEntity(DebitNote debitNote) {
        DebitNoteEntity entity = new DebitNoteEntity();
        entity.setId(debitNote.getId().value());
        entity.setNoteNumber(debitNote.getNoteNumber().value());
        entity.setCustomerId(debitNote.getCustomerId());
        entity.setInvoiceId(debitNote.getInvoiceId());
        entity.setAmount(debitNote.getAmount());
        entity.setReason(debitNote.getReason());
        entity.setCreatedBy(debitNote.getCreatedBy());
        entity.setCreatedByName(debitNote.getCreatedByName());
        entity.setCreatedAt(debitNote.getCreatedAt());
        entity.setUpdatedAt(debitNote.getUpdatedAt());
        return entity;
    }

    public static DebitNote toDomain(DebitNoteEntity entity) {
        return DebitNote.reconstitute(
                DebitNoteId.of(entity.getId()),
                new DebitNoteNumber(entity.getNoteNumber()),
                entity.getCustomerId(),
                entity.getInvoiceId(),
                entity.getAmount(),
                entity.getReason(),
                entity.getCreatedBy(),
                entity.getCreatedByName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
