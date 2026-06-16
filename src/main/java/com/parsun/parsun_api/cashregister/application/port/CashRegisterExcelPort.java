package com.parsun.parsun_api.cashregister.application.port;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterExportRow;
import com.parsun.parsun_api.cashregister.application.dto.CustomerSummary;
import com.parsun.parsun_api.cashregister.application.dto.DailyTotals;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegisterDetail;

import java.util.List;

public interface CashRegisterExcelPort {
    byte[] generateExcel(List<CashRegisterExportRow> items);

    byte[] generateSimpleExcel(List<CashRegisterDetail> paymentSummary, DailyTotals totals);

    byte[] generateGroupedExcel(List<CustomerSummary> customerSummaries);
}
