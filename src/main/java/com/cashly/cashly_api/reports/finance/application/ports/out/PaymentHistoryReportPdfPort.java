package com.cashly.cashly_api.reports.finance.application.ports.out;

import com.cashly.cashly_api.reports.finance.application.dto.PaymentHistoryReportData;

public interface PaymentHistoryReportPdfPort {
    byte[] generateReport(PaymentHistoryReportData data);
}
