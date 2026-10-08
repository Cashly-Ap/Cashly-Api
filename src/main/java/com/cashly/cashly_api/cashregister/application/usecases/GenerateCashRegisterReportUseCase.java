package com.cashly.cashly_api.cashregister.application.usecases;

import com.cashly.cashly_api.cashregister.application.dto.CashRegisterMapper;
import com.cashly.cashly_api.cashregister.application.dto.ReportFormat;
import com.cashly.cashly_api.cashregister.application.dto.ReportType;
import com.cashly.cashly_api.cashregister.application.port.CashRegisterReportPort;
import com.cashly.cashly_api.cashregister.application.contracts.CashRegisterQueryGateway;
import com.cashly.cashly_api.cashregister.domain.repositories.CashRegisterRepository;
import com.cashly.cashly_api.cashregister.domain.valueobjects.RegisterId;

import java.util.UUID;

public class GenerateCashRegisterReportUseCase {

    private final CashRegisterRepository repository;
    private final CashRegisterQueryGateway queries;
    private final CashRegisterReportPort reportPort;

    public GenerateCashRegisterReportUseCase(CashRegisterRepository repository, CashRegisterQueryGateway queries,
                                             CashRegisterReportPort reportPort) {
        this.repository = repository;
        this.queries = queries;
        this.reportPort = reportPort;
    }

    public byte[] execute(UUID registerId, ReportFormat format, ReportType type) {
        var register = repository.findById(RegisterId.of(registerId))
                .orElseThrow(() -> new IllegalArgumentException("Cash register not found: " + registerId));
        var movements = repository.findMovements(register.getId());
        var response = CashRegisterMapper.toResponse(register);
        if (!movements.isEmpty()) {
            var totals = queries.findSessionTotals(register.getId());
            register.applyMovementTotals(totals.totalRevenue(), totals.totalExpenses());
            response = CashRegisterMapper.toResponse(register, totals, movements);
        }
        return format == ReportFormat.PDF
                ? reportPort.generatePdf(response, type)
                : reportPort.generateExcel(response, type);
    }
}
