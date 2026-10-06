package com.cashly.cashly_api.cashregister.domain.repositories;

import com.cashly.cashly_api.cashregister.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cashregister.application.dto.CashRegisterReportFilter;
import com.cashly.cashly_api.cashregister.application.dto.CustomerSummary;
import com.cashly.cashly_api.cashregister.application.dto.DailyTotals;
import com.cashly.cashly_api.cashregister.domain.entities.CashRegister;
import com.cashly.cashly_api.cashregister.domain.entities.CashRegisterDetail;
import com.cashly.cashly_api.cashregister.domain.entities.CashMovement;
import com.cashly.cashly_api.cashregister.domain.valueobjects.RegisterId;
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

    DailyTotals findSessionTotals(RegisterId registerId);

    CashMovement saveMovement(CashMovement movement);

    List<CashMovement> findMovements(RegisterId registerId);

    boolean existsMovement(String sourceType, java.util.UUID sourceId);

    DailyTotals findRangeTotals(LocalDate dateFrom, LocalDate dateTo);

    List<CashRegisterExportRow> findExportRows(LocalDate dateFrom, LocalDate dateTo);

    List<CashRegisterExportRow> findExportRows(CashRegisterReportFilter filter);

    List<CashRegisterDetail> findPaymentMethodSummary(LocalDate dateFrom, LocalDate dateTo);

    List<CustomerSummary> findCustomerSummary(LocalDate dateFrom, LocalDate dateTo);
}
