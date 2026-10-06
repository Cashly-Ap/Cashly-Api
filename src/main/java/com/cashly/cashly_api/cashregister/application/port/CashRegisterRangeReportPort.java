package com.cashly.cashly_api.cashregister.application.port;

import com.cashly.cashly_api.cashregister.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cashregister.application.dto.CashRegisterReportFilter;
import com.cashly.cashly_api.cashregister.application.dto.ReportFormat;
import com.cashly.cashly_api.cashregister.application.dto.ReportType;

import java.util.List;

public interface CashRegisterRangeReportPort {
    byte[] generate(List<CashRegisterExportRow> rows, CashRegisterReportFilter filter,
                    ReportFormat format, ReportType type);
}
