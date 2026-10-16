package com.cashly.cashly_api.receivables.reporting.application.generate_collections_report_pdf;

import java.time.LocalDate;
import java.util.UUID;

public interface GenerateCollectionsReportPdfUseCase {
    byte[] execute(UUID customerId, LocalDate agingFrom);
}
