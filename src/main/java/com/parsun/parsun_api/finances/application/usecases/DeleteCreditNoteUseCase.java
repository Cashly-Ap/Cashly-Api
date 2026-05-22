package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.domain.entities.CreditNote;
import com.parsun.parsun_api.finances.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.CreditNoteId;
import com.parsun.parsun_api.finances.domain.valueobjects.CreditNoteStatus;
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
        CreditNote creditNote = creditNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Credit note not found: " + noteId));

        if (creditNote.getStatus() != CreditNoteStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT credit notes can be deleted");
        }

        creditNoteRepository.delete(id);
    }
}
