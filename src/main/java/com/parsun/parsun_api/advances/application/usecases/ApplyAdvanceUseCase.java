package com.parsun.parsun_api.advances.application.usecases;

import com.parsun.parsun_api.advances.application.dto.request.ApplyAdvanceRequest;
import com.parsun.parsun_api.advances.application.dto.response.AdvanceResponse;
import com.parsun.parsun_api.advances.application.mappers.AdvanceMapper;
import com.parsun.parsun_api.advances.domain.entities.Advance;
import com.parsun.parsun_api.advances.domain.entities.AdvanceConsumption;
import com.parsun.parsun_api.advances.domain.repositories.AdvanceConsumptionRepository;
import com.parsun.parsun_api.advances.domain.repositories.AdvanceRepository;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceId;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceStatus;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class ApplyAdvanceUseCase {

    private final AdvanceRepository advanceRepository;
    private final AdvanceConsumptionRepository consumptionRepository;

    public ApplyAdvanceUseCase(AdvanceRepository advanceRepository,
                                AdvanceConsumptionRepository consumptionRepository) {
        this.advanceRepository = advanceRepository;
        this.consumptionRepository = consumptionRepository;
    }

    @Transactional
    public AdvanceResponse execute(UUID advanceId, ApplyAdvanceRequest request) {
        Advance advance = advanceRepository.findById(AdvanceId.of(advanceId))
                .orElseThrow(() -> new IllegalArgumentException("Advance not found: " + advanceId));

        if (advance.getStatus() == AdvanceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot apply a cancelled advance");
        }

        // Create consumption record (can reference invoice and/or payment)
        AdvanceConsumption consumption = AdvanceConsumption.create(
                advanceId,
                request.invoiceId(),
                request.amount(),
                null
        );
        consumptionRepository.save(consumption);

        // Update advance state
        advance.applyConsumption(request.amount());
        advanceRepository.save(advance);

        var consumptions = consumptionRepository.findByAdvanceId(advanceId);
        return AdvanceMapper.toResponse(advance, consumptions);
    }
}
