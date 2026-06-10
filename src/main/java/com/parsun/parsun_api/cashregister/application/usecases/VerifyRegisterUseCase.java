package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterMapper;
import com.parsun.parsun_api.cashregister.application.dto.CashRegisterResponse;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegister;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;

import java.util.UUID;

public class VerifyRegisterUseCase {

    private final CashRegisterRepository closureRepository;

    public VerifyRegisterUseCase(CashRegisterRepository closureRepository) {
        this.closureRepository = closureRepository;
    }

    public CashRegisterResponse execute(UUID closureId, UUID verifiedBy, String verifiedByName) {
        CashRegister closure = closureRepository.findById(RegisterId.of(closureId))
                .orElseThrow(() -> new IllegalArgumentException("Closure not found: " + closureId));

        closure.verify(verifiedBy, verifiedByName);
        CashRegister saved = closureRepository.save(closure);
        return CashRegisterMapper.toResponse(saved);
    }
}
