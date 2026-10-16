package com.cashly.cashly_api.cash_register.reporting.application.ports.out;

import com.cashly.cashly_api.cash_register.reporting.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cash_register.reporting.application.dto.CustomerSummary;
import com.cashly.cashly_api.cash_register.sessions.application.dto.DailyTotals;
import com.cashly.cashly_api.cash_register.sessions.domain.CashRegisterDetail;

import java.util.List;

public interface CashRegisterExcelPort {
    byte[] generateExcel(List<CashRegisterExportRow> items);

    byte[] generateSimpleExcel(List<CashRegisterDetail> paymentSummary, DailyTotals totals);

    byte[] generateGroupedExcel(List<CustomerSummary> customerSummaries);
}
