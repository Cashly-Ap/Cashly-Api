package com.parsun.parsun_api.debit_notes.application.usecases;

import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteId;
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
        debitNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Debit note not found: " + noteId));

        debitNoteRepository.delete(id);
    }
}
