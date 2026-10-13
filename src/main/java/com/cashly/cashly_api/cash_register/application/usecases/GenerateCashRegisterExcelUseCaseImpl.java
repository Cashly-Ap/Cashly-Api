package com.cashly.cashly_api.cash_register.application.usecases;

import com.cashly.cashly_api.cash_register.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cash_register.application.dto.CustomerSummary;
import com.cashly.cashly_api.cash_register.application.dto.DailyTotals;
import com.cashly.cashly_api.cash_register.application.dto.ReportType;
import com.cashly.cashly_api.cash_register.application.ports.out.CashRegisterExcelPort;
import com.cashly.cashly_api.cash_register.domain.entities.CashRegisterDetail;
import com.cashly.cashly_api.cash_register.application.ports.out.CashRegisterQueryGateway;

import java.time.LocalDate;
import java.util.List;

public class GenerateCashRegisterExcelUseCaseImpl implements GenerateCashRegisterExcelUseCase {

    private final CashRegisterQueryGateway repository;
    private final CashRegisterExcelPort excelPort;

    public GenerateCashRegisterExcelUseCaseImpl(CashRegisterQueryGateway repository,
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
