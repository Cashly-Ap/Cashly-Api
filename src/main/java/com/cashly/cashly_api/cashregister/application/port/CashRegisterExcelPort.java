package com.cashly.cashly_api.cashregister.application.port;

import com.cashly.cashly_api.cashregister.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cashregister.application.dto.CustomerSummary;
import com.cashly.cashly_api.cashregister.application.dto.DailyTotals;
import com.cashly.cashly_api.cashregister.domain.entities.CashRegisterDetail;

import java.util.List;

public interface CashRegisterExcelPort {
    byte[] generateExcel(List<CashRegisterExportRow> items);

    byte[] generateSimpleExcel(List<CashRegisterDetail> paymentSummary, DailyTotals totals);

    byte[] generateGroupedExcel(List<CustomerSummary> customerSummaries);
}
