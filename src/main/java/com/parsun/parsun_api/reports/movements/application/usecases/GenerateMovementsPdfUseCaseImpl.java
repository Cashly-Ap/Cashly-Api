package com.parsun.parsun_api.reports.movements.application.usecases;

import com.parsun.parsun_api.reports.movements.application.dto.response.MovementReportResponse;
import com.parsun.parsun_api.reports.movements.application.port.MovementsPdfPort;
import com.parsun.parsun_api.reports.movements.application.port.MovementsReportRepositoryPort;
import com.parsun.parsun_api.reports.movements.application.query.MovementsPdfQuery;

import java.util.List;

public class GenerateMovementsPdfUseCaseImpl implements GenerateMovementsPdfUseCase {

    private final MovementsReportRepositoryPort repository;
    private final MovementsPdfPort pdfPort;

    public GenerateMovementsPdfUseCaseImpl(MovementsReportRepositoryPort repository, MovementsPdfPort pdfPort) {
        this.repository = repository;
        this.pdfPort = pdfPort;
    }

    @Override
    public byte[] execute(MovementsPdfQuery query) {
        List<MovementReportResponse> items = repository.findAllForPdf(query);
        return pdfPort.generatePdf(items);
    }
}
