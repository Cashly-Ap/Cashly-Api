package com.parsun.parsun_api.reports.finance.application.usecases;

import com.parsun.parsun_api.reports.finance.application.dto.CollectionsReportData;
import com.parsun.parsun_api.reports.finance.application.port.CollectionsReportPdfPort;

import java.time.LocalDate;
import java.util.UUID;

public class GenerateCollectionsReportPdfUseCaseImpl implements GenerateCollectionsReportPdfUseCase {

    private final CollectionsReportPdfPort pdfPort;

    public GenerateCollectionsReportPdfUseCaseImpl(CollectionsReportPdfPort pdfPort) {
        this.pdfPort = pdfPort;
    }

    @Override
    public byte[] execute(UUID customerId, LocalDate agingFrom) {
        CollectionsReportData data = buildReportData(customerId, agingFrom);
        return pdfPort.generateReport(data);
    }

    private CollectionsReportData buildReportData(UUID customerId, LocalDate agingFrom) {
        return new CollectionsReportData(
                LocalDate.now(),
                customerId,
                agingFrom,
                java.util.List.of(),
                new CollectionsReportData.Summary(
                        0,
                        java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO
                )
        );
    }
}
