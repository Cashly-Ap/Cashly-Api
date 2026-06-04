package com.parsun.parsun_api.advances.application.usecases;

import com.parsun.parsun_api.advances.application.dto.response.AdvanceResponse;
import com.parsun.parsun_api.advances.application.mappers.AdvanceMapper;
import com.parsun.parsun_api.advances.domain.entities.Advance;
import com.parsun.parsun_api.advances.domain.repositories.AdvanceConsumptionRepository;
import com.parsun.parsun_api.advances.domain.repositories.AdvanceRepository;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceId;

import java.util.UUID;

public class GetAdvanceByIdUseCase {

    private final AdvanceRepository advanceRepository;
    private final AdvanceConsumptionRepository consumptionRepository;

    public GetAdvanceByIdUseCase(AdvanceRepository advanceRepository,
                                  AdvanceConsumptionRepository consumptionRepository) {
        this.advanceRepository = advanceRepository;
        this.consumptionRepository = consumptionRepository;
    }

    public AdvanceResponse execute(UUID id) {
        Advance advance = advanceRepository.findById(AdvanceId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Advance not found: " + id));

        var consumptions = consumptionRepository.findByAdvanceId(id);

        return AdvanceMapper.toResponse(advance, consumptions);
    }
}
