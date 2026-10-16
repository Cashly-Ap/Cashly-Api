package com.cashly.cashly_api.cash_register.reporting.application.ports.out;

import com.cashly.cashly_api.cash_register.reporting.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cash_register.reporting.application.dto.CashRegisterReportFilter;
import com.cashly.cashly_api.cash_register.reporting.application.dto.ReportFormat;
import com.cashly.cashly_api.cash_register.reporting.application.dto.ReportType;

import java.util.List;

public interface CashRegisterRangeReportPort {
    byte[] generate(List<CashRegisterExportRow> rows, CashRegisterReportFilter filter,
                    ReportFormat format, ReportType type);
}
