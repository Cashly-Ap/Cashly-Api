package com.cashly.cashly_api.payments.reporting.application.generate_payment_history_report_pdf;

import java.time.LocalDate;
import java.util.UUID;

public interface GeneratePaymentHistoryReportPdfUseCase {
    byte[] execute(LocalDate dateFrom, LocalDate dateTo, UUID customerId, String type, String method);
}
