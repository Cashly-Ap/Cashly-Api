package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.CreditNoteId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteCreditNoteUseCase {

    private final CreditNoteRepository creditNoteRepository;

    public DeleteCreditNoteUseCase(CreditNoteRepository creditNoteRepository) {
        this.creditNoteRepository = creditNoteRepository;
    }

    @Transactional
    public void execute(UUID noteId) {
        CreditNoteId id = new CreditNoteId(noteId);
        creditNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Credit note not found: " + noteId));

        creditNoteRepository.delete(id);
    }
}
