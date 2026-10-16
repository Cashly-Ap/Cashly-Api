package com.cashly.cashly_api.credit_notes.infrastructure.persistence.mappers;

import com.cashly.cashly_api.credit_notes.domain.entity.CreditNote;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import com.cashly.cashly_api.credit_notes.infrastructure.persistence.entities.CreditNoteEntity;

public class CreditNoteJpaMapper {

    private CreditNoteJpaMapper() {}

    public static CreditNoteEntity toEntity(CreditNote creditNote) {
        CreditNoteEntity entity = new CreditNoteEntity();
        entity.setId(creditNote.getId().value());
        entity.setNoteNumber(creditNote.getNoteNumber().value());
        entity.setCustomerId(creditNote.getCustomerId());
        entity.setInvoiceId(creditNote.getInvoiceId());
        entity.setAmount(creditNote.getAmount());
        entity.setReason(creditNote.getReason());
        entity.setCreatedBy(creditNote.getCreatedBy());
        entity.setCreatedByName(creditNote.getCreatedByName());
        entity.setCreatedAt(creditNote.getCreatedAt());
        entity.setUpdatedAt(creditNote.getUpdatedAt());
        return entity;
    }

    public static CreditNote toDomain(CreditNoteEntity entity) {
        return CreditNote.reconstitute(
                CreditNoteId.of(entity.getId()),
                new CreditNoteNumber(entity.getNoteNumber()),
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
