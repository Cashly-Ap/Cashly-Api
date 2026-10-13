package com.cashly.cashly_api.cash_register.application.ports.out;

import com.cashly.cashly_api.cash_register.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cash_register.application.dto.CashRegisterReportFilter;
import com.cashly.cashly_api.cash_register.application.dto.ReportFormat;
import com.cashly.cashly_api.cash_register.application.dto.ReportType;

import java.util.List;

public interface CashRegisterRangeReportPort {
    byte[] generate(List<CashRegisterExportRow> rows, CashRegisterReportFilter filter,
                    ReportFormat format, ReportType type);
}
