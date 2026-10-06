package com.cashly.cashly_api.cashregister.application.usecases;

import com.cashly.cashly_api.cashregister.application.dto.CashRegisterMapper;
import com.cashly.cashly_api.cashregister.application.dto.CashRegisterResponse;
import com.cashly.cashly_api.cashregister.domain.entities.CashRegister;
import com.cashly.cashly_api.cashregister.domain.repositories.CashRegisterRepository;
import com.cashly.cashly_api.cashregister.domain.valueobjects.RegisterId;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

public class OpenRegisterUseCase {

    private final CashRegisterRepository closureRepository;

    public OpenRegisterUseCase(CashRegisterRepository closureRepository) {
        this.closureRepository = closureRepository;
    }

    public CashRegisterResponse execute(UUID openedBy, String openedByName, BigDecimal openingBalance) {
        LocalDate today = LocalDate.now();

        if (closureRepository.findActiveClosure().isPresent()) {
            throw new IllegalStateException("A cash register is already open");
        }

        CashRegister closure = CashRegister.open(RegisterId.generate(), today, openedBy,
                openedByName, openingBalance);
        CashRegister saved = closureRepository.save(closure);
        return CashRegisterMapper.toResponse(saved);
    }
}
