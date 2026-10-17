package com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out;

import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterExportRow;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CustomerSummary;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.DailyTotals;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegisterDetail;

import java.util.List;

public interface CashRegisterExcelPort {
    byte[] generateExcel(List<CashRegisterExportRow> items);

    byte[] generateSimpleExcel(List<CashRegisterDetail> paymentSummary, DailyTotals totals);

    byte[] generateGroupedExcel(List<CustomerSummary> customerSummaries);
}
