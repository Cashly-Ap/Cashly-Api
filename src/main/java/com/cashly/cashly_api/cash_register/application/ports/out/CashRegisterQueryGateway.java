package com.cashly.cashly_api.cash_register.application.ports.out;

import com.cashly.cashly_api.cash_register.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cash_register.application.dto.CashRegisterReportFilter;
import com.cashly.cashly_api.cash_register.application.dto.CustomerSummary;
import com.cashly.cashly_api.cash_register.application.dto.DailyTotals;
import com.cashly.cashly_api.cash_register.domain.entities.CashRegister;
import com.cashly.cashly_api.cash_register.domain.entities.CashRegisterDetail;
import com.cashly.cashly_api.cash_register.domain.valueobjects.RegisterId;
import com.cashly.cashly_api.shared.dto.PageResponse;
import java.time.LocalDate;
import java.util.List;

public interface CashRegisterQueryGateway {
    PageResponse<CashRegister> findAll(int page, int size, String sort, LocalDate dateFrom, LocalDate dateTo);
    DailyTotals findDailyTotals(LocalDate closureDate);
    DailyTotals findSessionTotals(RegisterId registerId);
    DailyTotals findRangeTotals(LocalDate dateFrom, LocalDate dateTo);
    List<CashRegisterExportRow> findExportRows(LocalDate dateFrom, LocalDate dateTo);
    List<CashRegisterExportRow> findExportRows(CashRegisterReportFilter filter);
    List<CashRegisterDetail> findPaymentMethodSummary(LocalDate dateFrom, LocalDate dateTo);
    List<CustomerSummary> findCustomerSummary(LocalDate dateFrom, LocalDate dateTo);
}
