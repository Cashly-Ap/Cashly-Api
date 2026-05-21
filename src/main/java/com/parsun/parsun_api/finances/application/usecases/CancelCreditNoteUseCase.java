package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.CreditNote;
import com.parsun.parsun_api.finances.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.CreditNoteId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelCreditNoteUseCase {

    private final CreditNoteRepository creditNoteRepository;

    public CancelCreditNoteUseCase(CreditNoteRepository creditNoteRepository) {
        this.creditNoteRepository = creditNoteRepository;
    }

    @Transactional
    public CreditNoteResponse execute(UUID noteId) {
        CreditNoteId id = new CreditNoteId(noteId);
        CreditNote creditNote = creditNoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Credit note not found: " + noteId));

        creditNote.cancel();

        CreditNote saved = creditNoteRepository.save(creditNote);

        return FinanceMapper.toCreditNoteResponse(saved, null, null);
    }
}
