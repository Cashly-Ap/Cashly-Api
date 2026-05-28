package com.parsun.parsun_api.receivables.application.usecases;

import com.parsun.parsun_api.receivables.application.dto.response.ReceivableResponse;
import com.parsun.parsun_api.receivables.application.mappers.ReceivableMapper;
import com.parsun.parsun_api.receivables.domain.repositories.ReceivableRepository;
import com.parsun.parsun_api.receivables.domain.valueobjects.ReceivableStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetAllReceivablesUseCase {

    private final ReceivableRepository receivableRepository;

    public GetAllReceivablesUseCase(ReceivableRepository receivableRepository) {
        this.receivableRepository = receivableRepository;
    }

    public List<ReceivableResponse> execute(UUID customerId, List<String> statuses,
                                             LocalDate dueDateFrom, LocalDate dueDateTo,
                                             String orderBy, String orderDir) {
        List<ReceivableStatus> effectiveStatuses = (statuses == null || statuses.isEmpty())
                ? List.of(ReceivableStatus.PENDING, ReceivableStatus.PARTIALLY_PAID, ReceivableStatus.OVERDUE)
                : statuses.stream().map(ReceivableStatus::valueOf).toList();

        return receivableRepository.findAllWithFilters(customerId, effectiveStatuses,
                        dueDateFrom, dueDateTo, orderBy, orderDir)
                .stream()
                .map(ReceivableMapper::toResponse)
                .toList();
    }
}
