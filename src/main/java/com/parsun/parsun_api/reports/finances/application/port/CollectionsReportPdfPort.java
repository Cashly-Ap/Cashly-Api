package com.parsun.parsun_api.reports.finances.application.port;

import com.parsun.parsun_api.reports.finances.application.dto.CollectionsReportData;

public interface CollectionsReportPdfPort {
    byte[] generateReport(CollectionsReportData data);
}
