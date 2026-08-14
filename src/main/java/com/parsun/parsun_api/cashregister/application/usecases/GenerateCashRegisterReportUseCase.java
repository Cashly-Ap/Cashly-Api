package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterMapper;
import com.parsun.parsun_api.cashregister.application.dto.ReportFormat;
import com.parsun.parsun_api.cashregister.application.dto.ReportType;
import com.parsun.parsun_api.cashregister.application.port.CashRegisterReportPort;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;

import java.util.UUID;

public class GenerateCashRegisterReportUseCase {

    private final CashRegisterRepository repository;
    private final CashRegisterReportPort reportPort;

    public GenerateCashRegisterReportUseCase(CashRegisterRepository repository,
                                             CashRegisterReportPort reportPort) {
        this.repository = repository;
        this.reportPort = reportPort;
    }

    public byte[] execute(UUID registerId, ReportFormat format, ReportType type) {
        var register = repository.findById(RegisterId.of(registerId))
                .orElseThrow(() -> new IllegalArgumentException("Cash register not found: " + registerId));
        var movements = repository.findMovements(register.getId());
        var response = CashRegisterMapper.toResponse(register);
        if (!movements.isEmpty()) {
            var totals = repository.findSessionTotals(register.getId());
            register.applyMovementTotals(totals.totalRevenue(), totals.totalExpenses());
            response = CashRegisterMapper.toResponse(register, totals, movements);
        }
        return format == ReportFormat.PDF
                ? reportPort.generatePdf(response, type)
                : reportPort.generateExcel(response, type);
    }
}
