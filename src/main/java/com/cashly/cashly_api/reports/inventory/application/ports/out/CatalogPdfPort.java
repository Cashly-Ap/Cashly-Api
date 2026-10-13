package com.cashly.cashly_api.reports.inventory.application.ports.out;

import com.cashly.cashly_api.reports.inventory.application.dto.response.CatalogReportResponse;

import java.util.List;

public interface CatalogPdfPort {
    byte[] generatePdf(List<CatalogReportResponse> items);
}
