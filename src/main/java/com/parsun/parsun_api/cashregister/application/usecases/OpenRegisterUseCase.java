package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterMapper;
import com.parsun.parsun_api.cashregister.application.dto.CashRegisterResponse;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegister;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;

import java.time.LocalDate;
import java.util.UUID;

public class OpenRegisterUseCase {

    private final CashRegisterRepository closureRepository;

    public OpenRegisterUseCase(CashRegisterRepository closureRepository) {
        this.closureRepository = closureRepository;
    }

    public CashRegisterResponse execute(UUID openedBy, String openedByName) {
        LocalDate today = LocalDate.now();

        if (closureRepository.findByClosureDate(today).isPresent()) {
            throw new IllegalStateException("A closure already exists for today: " + today);
        }

        CashRegister closure = CashRegister.open(RegisterId.generate(), today, openedBy, openedByName);
        CashRegister saved = closureRepository.save(closure);
        return CashRegisterMapper.toResponse(saved);
    }
}
