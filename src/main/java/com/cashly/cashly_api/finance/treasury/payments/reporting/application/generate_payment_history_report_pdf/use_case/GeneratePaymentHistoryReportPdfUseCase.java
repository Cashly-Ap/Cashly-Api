package com.cashly.cashly_api.finance.treasury.payments.reporting.application.generate_payment_history_report_pdf.use_case;

import java.time.LocalDate;
import java.util.UUID;

public interface GeneratePaymentHistoryReportPdfUseCase {
    byte[] execute(LocalDate dateFrom, LocalDate dateTo, UUID customerId,
            String type, String method);
}
