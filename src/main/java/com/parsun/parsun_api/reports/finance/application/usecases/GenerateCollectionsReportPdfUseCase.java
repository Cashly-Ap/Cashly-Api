package com.parsun.parsun_api.reports.finance.application.usecases;

import java.time.LocalDate;
import java.util.UUID;

public interface GenerateCollectionsReportPdfUseCase {
    byte[] execute(UUID customerId, LocalDate agingFrom);
}
