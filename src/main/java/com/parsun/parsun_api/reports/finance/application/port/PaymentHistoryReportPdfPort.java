package com.parsun.parsun_api.reports.finance.application.port;

import com.parsun.parsun_api.reports.finance.application.dto.PaymentHistoryReportData;

public interface PaymentHistoryReportPdfPort {
    byte[] generateReport(PaymentHistoryReportData data);
}
