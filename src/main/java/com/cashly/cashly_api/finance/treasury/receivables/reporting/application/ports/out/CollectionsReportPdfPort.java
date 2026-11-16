package com.cashly.cashly_api.finance.treasury.receivables.reporting.application.ports.out;

import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.dto.model.CollectionsReportData;

public interface CollectionsReportPdfPort {
    byte[] generateReport(CollectionsReportData data);
}
