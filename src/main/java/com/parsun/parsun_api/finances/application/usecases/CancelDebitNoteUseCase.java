package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.DebitNote;
import com.parsun.parsun_api.finances.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelDebitNoteUseCase {

    private final DebitNoteRepository debitNoteRepository;

    public CancelDebitNoteUseCase(DebitNoteRepository debitNoteRepository) {
        this.debitNoteRepository = debitNoteRepository;
    }

    @Transactional
    public DebitNoteResponse execute(UUID noteId) {
        DebitNoteId id = new DebitNoteId(noteId);
        DebitNote debitNote = debitNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debit note not found: " + noteId));

        debitNote.cancel();

        DebitNote saved = debitNoteRepository.save(debitNote);

        return FinanceMapper.toDebitNoteResponse(saved, null, null);
    }
}
