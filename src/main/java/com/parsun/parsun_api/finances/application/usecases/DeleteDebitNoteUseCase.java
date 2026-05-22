package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.domain.entities.DebitNote;
import com.parsun.parsun_api.finances.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteId;
import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteStatus;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteDebitNoteUseCase {

    private final DebitNoteRepository debitNoteRepository;

    public DeleteDebitNoteUseCase(DebitNoteRepository debitNoteRepository) {
        this.debitNoteRepository = debitNoteRepository;
    }

    @Transactional
    public void execute(UUID noteId) {
        DebitNoteId id = new DebitNoteId(noteId);
        DebitNote debitNote = debitNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debit note not found: " + noteId));

        if (debitNote.getStatus() != DebitNoteStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT debit notes can be deleted");
        }

        debitNoteRepository.delete(id);
    }
}
