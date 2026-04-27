package com.parsun.parsun_api.reports.inventory.application.port;

import com.parsun.parsun_api.reports.inventory.application.dto.response.CatalogReportResponse;

import java.util.List;

public interface CatalogPdfPort {
    byte[] generatePdf(List<CatalogReportResponse> items);
}
