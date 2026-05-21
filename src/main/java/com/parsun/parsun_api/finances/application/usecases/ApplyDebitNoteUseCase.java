package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.DebitNote;
import com.parsun.parsun_api.finances.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteId;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class ApplyDebitNoteUseCase {

    private final DebitNoteRepository debitNoteRepository;

    public ApplyDebitNoteUseCase(DebitNoteRepository debitNoteRepository) {
        this.debitNoteRepository = debitNoteRepository;
    }

    @Transactional
    public DebitNoteResponse execute(UUID noteId, UUID invoiceId, BigDecimal amount) {
        DebitNoteId id = new DebitNoteId(noteId);
        DebitNote debitNote = debitNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debit note not found: " + noteId));

        debitNote.apply(amount);

        DebitNote saved = debitNoteRepository.save(debitNote);

        return FinanceMapper.toDebitNoteResponse(saved, null, null);
    }
}
