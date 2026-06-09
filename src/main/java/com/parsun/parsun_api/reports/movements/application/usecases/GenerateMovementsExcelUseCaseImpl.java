package com.parsun.parsun_api.reports.movements.application.usecases;

import com.parsun.parsun_api.reports.movements.application.dto.response.MovementReportResponse;
import com.parsun.parsun_api.reports.movements.application.port.MovementsExcelPort;
import com.parsun.parsun_api.reports.movements.application.port.MovementsReportRepositoryPort;
import com.parsun.parsun_api.reports.movements.application.query.MovementsPdfQuery;

import java.util.List;

public class GenerateMovementsExcelUseCaseImpl implements GenerateMovementsExcelUseCase {

    private final MovementsReportRepositoryPort repository;
    private final MovementsExcelPort excelPort;

    public GenerateMovementsExcelUseCaseImpl(MovementsReportRepositoryPort repository, MovementsExcelPort excelPort) {
        this.repository = repository;
        this.excelPort = excelPort;
    }

    @Override
    public byte[] execute(MovementsPdfQuery query) {
        List<MovementReportResponse> items = repository.findAllForPdf(query);
        return excelPort.generateExcel(items);
    }
}
