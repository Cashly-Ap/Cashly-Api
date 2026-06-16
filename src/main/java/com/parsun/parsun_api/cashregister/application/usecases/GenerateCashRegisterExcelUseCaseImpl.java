package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterExportRow;
import com.parsun.parsun_api.cashregister.application.dto.CustomerSummary;
import com.parsun.parsun_api.cashregister.application.dto.DailyTotals;
import com.parsun.parsun_api.cashregister.application.dto.ReportType;
import com.parsun.parsun_api.cashregister.application.port.CashRegisterExcelPort;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegisterDetail;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;

import java.time.LocalDate;
import java.util.List;

public class GenerateCashRegisterExcelUseCaseImpl implements GenerateCashRegisterExcelUseCase {

    private final CashRegisterRepository repository;
    private final CashRegisterExcelPort excelPort;

    public GenerateCashRegisterExcelUseCaseImpl(CashRegisterRepository repository,
                                                 CashRegisterExcelPort excelPort) {
        this.repository = repository;
        this.excelPort = excelPort;
    }

    @Override
    public byte[] execute(LocalDate dateFrom, LocalDate dateTo, ReportType type) {
        return switch (type) {
            case SIMPLE -> generateSimple(dateFrom, dateTo);
            case DETAILED -> generateDetailed(dateFrom, dateTo);
            case GROUPED -> generateGrouped(dateFrom, dateTo);
        };
    }

    private byte[] generateSimple(LocalDate dateFrom, LocalDate dateTo) {
        List<CashRegisterDetail> paymentSummary = repository.findPaymentMethodSummary(dateFrom, dateTo);
        DailyTotals totals = repository.findRangeTotals(dateFrom, dateTo);
        return excelPort.generateSimpleExcel(paymentSummary, totals);
    }

    private byte[] generateDetailed(LocalDate dateFrom, LocalDate dateTo) {
        List<CashRegisterExportRow> rows = repository.findExportRows(dateFrom, dateTo);
        return excelPort.generateExcel(rows);
    }

    private byte[] generateGrouped(LocalDate dateFrom, LocalDate dateTo) {
        List<CustomerSummary> summaries = repository.findCustomerSummary(dateFrom, dateTo);
        return excelPort.generateGroupedExcel(summaries);
    }
}
