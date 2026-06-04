package com.parsun.parsun_api.advances.application.usecases;

import com.parsun.parsun_api.advances.domain.entities.Advance;
import com.parsun.parsun_api.advances.domain.repositories.AdvanceRepository;
import com.parsun.parsun_api.advances.domain.valueobjects.AdvanceId;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class CancelAdvanceUseCase {

    private final AdvanceRepository advanceRepository;

    public CancelAdvanceUseCase(AdvanceRepository advanceRepository) {
        this.advanceRepository = advanceRepository;
    }

    @Transactional
    public void execute(UUID id) {
        Advance advance = advanceRepository.findById(AdvanceId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Advance not found: " + id));

        advance.cancel();
        advanceRepository.save(advance);
    }
}
