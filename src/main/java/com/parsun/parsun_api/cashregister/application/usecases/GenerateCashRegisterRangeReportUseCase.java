package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterReportFilter;
import com.parsun.parsun_api.cashregister.application.dto.ReportFormat;
import com.parsun.parsun_api.cashregister.application.dto.ReportType;
import com.parsun.parsun_api.cashregister.application.port.CashRegisterRangeReportPort;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;

public class GenerateCashRegisterRangeReportUseCase {

    private final CashRegisterRepository repository;
    private final CashRegisterRangeReportPort reportPort;

    public GenerateCashRegisterRangeReportUseCase(CashRegisterRepository repository,
                                                   CashRegisterRangeReportPort reportPort) {
        this.repository = repository;
        this.reportPort = reportPort;
    }

    public byte[] execute(CashRegisterReportFilter filter, ReportFormat format, ReportType type) {
        return reportPort.generate(repository.findExportRows(filter), filter, format, type);
    }
}
