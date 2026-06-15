package com.parsun.parsun_api.cashregister.domain.repositories;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterExportRow;
import com.parsun.parsun_api.cashregister.application.dto.DailyTotals;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegister;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CashRegisterRepository {

    CashRegister save(CashRegister closure);

    Optional<CashRegister> findById(RegisterId id);

    Optional<CashRegister> findByClosureDate(LocalDate date);

    Optional<CashRegister> findActiveClosure();

    Page<CashRegister> findAll(int page, int size, String sort,
                               LocalDate dateFrom, LocalDate dateTo);

    DailyTotals findDailyTotals(LocalDate closureDate);

    List<CashRegisterExportRow> findExportRows(LocalDate dateFrom, LocalDate dateTo);
}
