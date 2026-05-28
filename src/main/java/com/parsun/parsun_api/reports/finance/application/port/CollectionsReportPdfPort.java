package com.parsun.parsun_api.reports.finance.application.port;

import com.parsun.parsun_api.reports.finance.application.dto.CollectionsReportData;

public interface CollectionsReportPdfPort {
    byte[] generateReport(CollectionsReportData data);
}
