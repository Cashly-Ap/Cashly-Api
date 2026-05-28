package com.parsun.parsun_api.receivables.application.usecases;

import com.parsun.parsun_api.receivables.application.dto.response.ReceivableResponse;
import com.parsun.parsun_api.receivables.application.mappers.ReceivableMapper;
import com.parsun.parsun_api.receivables.domain.entities.Receivable;
import com.parsun.parsun_api.receivables.domain.repositories.ReceivableRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetAgingReportUseCase {

    private final ReceivableRepository receivableRepository;

    public GetAgingReportUseCase(ReceivableRepository receivableRepository) {
        this.receivableRepository = receivableRepository;
    }

    public ReceivableResponse.AgingReport execute() {
        List<Receivable> allReceivables = receivableRepository.findAllWithFilters(
                null, null, null, null, "dueDate", "asc"
        );

        Map<UUID, String> invoiceNumbers = Map.of();
        Map<UUID, UUID> invoiceCustomerIds = Map.of();
        Map<UUID, String> customerNames = Map.of();

        return ReceivableMapper.toAgingReport(allReceivables, invoiceNumbers, invoiceCustomerIds, customerNames);
    }
}
