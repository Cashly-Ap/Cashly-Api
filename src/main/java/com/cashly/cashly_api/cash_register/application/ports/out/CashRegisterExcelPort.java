package com.cashly.cashly_api.cash_register.application.ports.out;

import com.cashly.cashly_api.cash_register.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cash_register.application.dto.CustomerSummary;
import com.cashly.cashly_api.cash_register.application.dto.DailyTotals;
import com.cashly.cashly_api.cash_register.domain.entities.CashRegisterDetail;

import java.util.List;

public interface CashRegisterExcelPort {
    byte[] generateExcel(List<CashRegisterExportRow> items);

    byte[] generateSimpleExcel(List<CashRegisterDetail> paymentSummary, DailyTotals totals);

    byte[] generateGroupedExcel(List<CustomerSummary> customerSummaries);
}
