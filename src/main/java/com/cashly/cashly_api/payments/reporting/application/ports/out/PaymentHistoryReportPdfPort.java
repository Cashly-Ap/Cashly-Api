package com.cashly.cashly_api.payments.reporting.application.ports.out;

import com.cashly.cashly_api.payments.reporting.application.dto.model.PaymentHistoryReportData;

public interface PaymentHistoryReportPdfPort {
    byte[] generateReport(PaymentHistoryReportData data);
}
