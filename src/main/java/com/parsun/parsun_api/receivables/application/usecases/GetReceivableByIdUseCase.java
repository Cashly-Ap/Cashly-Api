package com.parsun.parsun_api.receivables.application.usecases;

import com.parsun.parsun_api.receivables.application.dto.response.ReceivableResponse;
import com.parsun.parsun_api.receivables.application.mappers.ReceivableMapper;
import com.parsun.parsun_api.receivables.domain.entities.Receivable;
import com.parsun.parsun_api.receivables.domain.repositories.ReceivableRepository;
import com.parsun.parsun_api.receivables.domain.valueobjects.ReceivableId;

import java.util.UUID;

public class GetReceivableByIdUseCase {

    private final ReceivableRepository receivableRepository;

    public GetReceivableByIdUseCase(ReceivableRepository receivableRepository) {
        this.receivableRepository = receivableRepository;
    }

    public ReceivableResponse execute(UUID id) {
        Receivable receivable = receivableRepository.findById(ReceivableId.of(id))
                .orElseThrow(() -> new RuntimeException("Receivable not found with id: " + id));
        return ReceivableMapper.toResponse(receivable);
    }
}
