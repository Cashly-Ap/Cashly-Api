package com.cashly.cashly_api.returns.application.usecases;

import com.cashly.cashly_api.returns.application.dto.response.ReturnListResponse;
import com.cashly.cashly_api.returns.application.mappers.ReturnMapper;
import com.cashly.cashly_api.returns.domain.repositories.ReturnRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetAllReturnsUseCase {

    private final ReturnRepository returnRepository;
    private final ReturnMapper returnMapper;

    public GetAllReturnsUseCase(ReturnRepository returnRepository, ReturnMapper returnMapper) {
        this.returnRepository = returnRepository;
        this.returnMapper = returnMapper;
    }

    public List<ReturnListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                             UUID customerId, UUID invoiceId) {
        return returnRepository.findAllWithFilters(dateFrom, dateTo, customerId, invoiceId)
                .stream()
                .map(returnMapper::toListResponse)
                .toList();
    }
}
