package com.parsun.parsun_api.reports.finances.application.port;

import com.parsun.parsun_api.reports.finances.application.dto.PaymentHistoryReportData;

public interface PaymentHistoryReportPdfPort {
    byte[] generateReport(PaymentHistoryReportData data);
}
