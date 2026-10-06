package com.cashly.cashly_api.reports.finance.application.port;

import com.cashly.cashly_api.reports.finance.application.dto.PaymentHistoryReportData;

public interface PaymentHistoryReportPdfPort {
    byte[] generateReport(PaymentHistoryReportData data);
}
