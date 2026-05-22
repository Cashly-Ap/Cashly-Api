package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.request.CreateCreditNoteRequest;
import com.parsun.parsun_api.finances.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.CreditNote;
import com.parsun.parsun_api.finances.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.CreditNoteNumber;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.UUID;

public class CreateCreditNoteUseCase {

    private final CreditNoteRepository creditNoteRepository;

    public CreateCreditNoteUseCase(CreditNoteRepository creditNoteRepository) {
        this.creditNoteRepository = creditNoteRepository;
    }

    @Transactional
    public CreditNoteResponse execute(CreateCreditNoteRequest request, UUID authenticatedUserId) {
        int year = LocalDate.now().getYear();
        int sequence = creditNoteRepository.getNextSequenceForYear(year);
        CreditNoteNumber noteNumber = CreditNoteNumber.of(year, sequence);

        CreditNote creditNote = CreditNote.create(
                noteNumber,
                request.customerId(),
                request.invoiceId(),
                request.amount(),
                request.reason(),
                authenticatedUserId,
                "User"
        );

        CreditNote saved = creditNoteRepository.save(creditNote);

        return FinanceMapper.toCreditNoteResponse(saved, null, null, null);
    }
}
