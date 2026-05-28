package com.parsun.parsun_api.debit_notes.application.usecases;

import com.parsun.parsun_api.debit_notes.application.dto.request.CreateDebitNoteRequest;
import com.parsun.parsun_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.debit_notes.application.mappers.DebitNoteMapper;
import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.UUID;

public class CreateDebitNoteUseCase {

    private final DebitNoteRepository debitNoteRepository;

    public CreateDebitNoteUseCase(DebitNoteRepository debitNoteRepository) {
        this.debitNoteRepository = debitNoteRepository;
    }

    @Transactional
    public DebitNoteResponse execute(CreateDebitNoteRequest request, UUID authenticatedUserId) {
        int year = LocalDate.now().getYear();
        int sequence = debitNoteRepository.getNextSequenceForYear(year);
        DebitNoteNumber noteNumber = DebitNoteNumber.of(year, sequence);

        DebitNote debitNote = DebitNote.create(
                noteNumber,
                request.customerId(),
                request.invoiceId(),
                request.amount(),
                request.reason(),
                authenticatedUserId,
                "User"
        );

        DebitNote saved = debitNoteRepository.save(debitNote);

        return DebitNoteMapper.toDebitNoteResponse(saved, null, null, null);
    }
}
