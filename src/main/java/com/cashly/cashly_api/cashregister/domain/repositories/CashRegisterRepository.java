package com.cashly.cashly_api.cashregister.domain.repositories;

import com.cashly.cashly_api.cashregister.domain.entities.CashRegister;
import com.cashly.cashly_api.cashregister.domain.entities.CashMovement;
import com.cashly.cashly_api.cashregister.domain.valueobjects.RegisterId;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashRegisterRepository {
    CashRegister save(CashRegister closure);
    Optional<CashRegister> findById(RegisterId id);
    Optional<CashRegister> findByClosureDate(LocalDate date);
    Optional<CashRegister> findActiveClosure();
    CashMovement saveMovement(CashMovement movement);
    List<CashMovement> findMovements(RegisterId registerId);
    boolean existsMovement(String sourceType, UUID sourceId);
}
