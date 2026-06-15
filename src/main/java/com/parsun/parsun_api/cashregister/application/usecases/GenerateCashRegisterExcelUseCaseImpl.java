package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterExportRow;
import com.parsun.parsun_api.cashregister.application.port.CashRegisterExcelPort;
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
    public byte[] execute(LocalDate dateFrom, LocalDate dateTo) {
        List<CashRegisterExportRow> rows = repository.findExportRows(dateFrom, dateTo);
        return excelPort.generateExcel(rows);
    }
}
