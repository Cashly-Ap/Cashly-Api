package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.CreditNote;
import com.parsun.parsun_api.finances.domain.repositories.CreditNoteRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetCreditNotesUseCase {

    private final CreditNoteRepository creditNoteRepository;

    public GetCreditNotesUseCase(CreditNoteRepository creditNoteRepository) {
        this.creditNoteRepository = creditNoteRepository;
    }

    public List<CreditNoteResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                             UUID customerId, String status) {
        List<CreditNote> creditNotes = creditNoteRepository.findAllWithFilters(dateFrom, dateTo, customerId, status);

        return creditNotes.stream()
                .map(cn -> FinanceMapper.toCreditNoteResponse(cn, null, null))
                .toList();
    }
}
