package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.DebitNoteResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.DebitNote;
import com.parsun.parsun_api.finances.domain.repositories.DebitNoteRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetDebitNotesUseCase {

    private final DebitNoteRepository debitNoteRepository;

    public GetDebitNotesUseCase(DebitNoteRepository debitNoteRepository) {
        this.debitNoteRepository = debitNoteRepository;
    }

    public List<DebitNoteResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                            UUID customerId, String status) {
        List<DebitNote> debitNotes = debitNoteRepository.findAllWithFilters(dateFrom, dateTo, customerId, status);

        return debitNotes.stream()
                .map(dn -> FinanceMapper.toDebitNoteResponse(dn, null, null))
                .toList();
    }
}
