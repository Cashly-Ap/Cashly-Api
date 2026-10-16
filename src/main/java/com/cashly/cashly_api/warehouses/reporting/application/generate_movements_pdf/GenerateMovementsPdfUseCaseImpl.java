package com.cashly.cashly_api.warehouses.reporting.application.generate_movements_pdf;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.MovementReportResponse;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.MovementsPdfPort;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.MovementsReportRepositoryPort;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.MovementsPdfQuery;

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
