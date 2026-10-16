package com.cashly.cashly_api.cash_register.application.ports.out;

import com.cashly.cashly_api.cash_register.reporting.application.dto.model.CashRegisterExportRow;
import com.cashly.cashly_api.cash_register.reporting.application.dto.model.CashRegisterReportFilter;
import com.cashly.cashly_api.cash_register.reporting.application.dto.model.CustomerSummary;
import com.cashly.cashly_api.cash_register.sessions.application.dto.response.DailyTotals;
import com.cashly.cashly_api.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.cash_register.sessions.domain.entity.CashRegisterDetail;
import com.cashly.cashly_api.cash_register.sessions.domain.valueobjects.RegisterId;
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
