package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.request.CreateDebitNoteRequest;
import com.parsun.parsun_api.finances.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.DebitNote;
import com.parsun.parsun_api.finances.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.DebitNoteNumber;
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

        return FinanceMapper.toDebitNoteResponse(saved, null, null);
    }
}
