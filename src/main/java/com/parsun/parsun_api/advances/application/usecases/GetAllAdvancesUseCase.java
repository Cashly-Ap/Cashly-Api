package com.parsun.parsun_api.advances.application.usecases;

import com.parsun.parsun_api.advances.application.dto.response.AdvanceListResponse;
import com.parsun.parsun_api.advances.application.mappers.AdvanceMapper;
import com.parsun.parsun_api.advances.domain.repositories.AdvanceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetAllAdvancesUseCase {

    private final AdvanceRepository advanceRepository;

    public GetAllAdvancesUseCase(AdvanceRepository advanceRepository) {
        this.advanceRepository = advanceRepository;
    }

    public List<AdvanceListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                              UUID customerId, String status) {
        return advanceRepository.findAllWithFilters(dateFrom, dateTo, customerId, status)
                .stream()
                .map(AdvanceMapper::toListResponse)
                .toList();
    }
}
