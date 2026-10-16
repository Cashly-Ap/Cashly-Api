package com.cashly.cashly_api.cash_register.reporting.application.range;

import com.cashly.cashly_api.cash_register.reporting.application.dto.CashRegisterReportFilter;
import com.cashly.cashly_api.cash_register.reporting.application.dto.ReportFormat;
import com.cashly.cashly_api.cash_register.reporting.application.dto.ReportType;
import com.cashly.cashly_api.cash_register.reporting.application.ports.out.CashRegisterRangeReportPort;
import com.cashly.cashly_api.cash_register.application.ports.out.CashRegisterQueryGateway;

public class GenerateCashRegisterRangeReportUseCase {

    private final CashRegisterQueryGateway repository;
    private final CashRegisterRangeReportPort reportPort;

    public GenerateCashRegisterRangeReportUseCase(CashRegisterQueryGateway repository,
                                                   CashRegisterRangeReportPort reportPort) {
        this.repository = repository;
        this.reportPort = reportPort;
    }

    public byte[] execute(CashRegisterReportFilter filter, ReportFormat format, ReportType type) {
        return reportPort.generate(repository.findExportRows(filter), filter, format, type);
    }
}
