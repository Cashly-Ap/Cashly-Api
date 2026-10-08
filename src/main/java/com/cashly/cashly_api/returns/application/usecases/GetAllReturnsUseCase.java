package com.cashly.cashly_api.returns.application.usecases;

import com.cashly.cashly_api.returns.application.dto.response.ReturnListResponse;
import com.cashly.cashly_api.returns.application.services.ReturnResponseAssembler;
import com.cashly.cashly_api.returns.domain.repositories.ReturnRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetAllReturnsUseCase {

    private final ReturnRepository returnRepository;
    private final ReturnResponseAssembler responses;

    public GetAllReturnsUseCase(ReturnRepository returnRepository, ReturnResponseAssembler responses) {
        this.returnRepository = returnRepository;
        this.responses = responses;
    }

    public List<ReturnListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                             UUID customerId, UUID invoiceId) {
        return returnRepository.findAllWithFilters(dateFrom, dateTo, customerId, invoiceId)
                .stream()
                .map(responses::toListResponse)
                .toList();
    }
}
