package com.cashly.cashly_api.finance.treasury.receivables.reporting.application.generate_collections_report_pdf.services;

import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.generate_collections_report_pdf.use_case.GenerateCollectionsReportPdfUseCase;

import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.dto.model.CollectionsReportData;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.ports.out.CollectionsReportPdfPort;

import java.time.LocalDate;
import java.util.UUID;

public class GenerateCollectionsReportPdfUseCaseImpl
        implements
            GenerateCollectionsReportPdfUseCase {

    private final CollectionsReportPdfPort pdfPort;

    public GenerateCollectionsReportPdfUseCaseImpl(
            CollectionsReportPdfPort pdfPort) {
        this.pdfPort = pdfPort;
    }

    @Override
    public byte[] execute(UUID customerId, LocalDate agingFrom) {
        CollectionsReportData data = buildReportData(customerId, agingFrom);
        return pdfPort.generateReport(data);
    }

    private CollectionsReportData buildReportData(UUID customerId,
            LocalDate agingFrom) {
        return new CollectionsReportData(LocalDate.now(), customerId, agingFrom,
                java.util.List.of(),
                new CollectionsReportData.Summary(0, java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO));
    }
}
