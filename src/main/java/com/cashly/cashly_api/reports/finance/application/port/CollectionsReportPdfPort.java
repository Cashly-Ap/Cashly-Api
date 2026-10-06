package com.cashly.cashly_api.reports.finance.application.port;

import com.cashly.cashly_api.reports.finance.application.dto.CollectionsReportData;

public interface CollectionsReportPdfPort {
    byte[] generateReport(CollectionsReportData data);
}
