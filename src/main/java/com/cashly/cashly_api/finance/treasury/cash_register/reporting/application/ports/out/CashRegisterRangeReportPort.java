package com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out;

import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterExportRow;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterReportFilter;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportFormat;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportType;

import java.util.List;

public interface CashRegisterRangeReportPort {
    byte[] generate(List<CashRegisterExportRow> rows,
            CashRegisterReportFilter filter, ReportFormat format,
            ReportType type);
}
