package com.parsun.parsun_api.reports.finance.application.usecases;

import java.time.LocalDate;
import java.util.UUID;

public interface GeneratePaymentHistoryReportPdfUseCase {
    byte[] execute(LocalDate dateFrom, LocalDate dateTo, UUID customerId, String type, String method);
}
