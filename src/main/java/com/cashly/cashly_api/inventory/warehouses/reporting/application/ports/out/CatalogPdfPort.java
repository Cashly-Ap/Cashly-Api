package com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.CatalogReportResponse;

import java.util.List;

public interface CatalogPdfPort {
    byte[] generatePdf(List<CatalogReportResponse> items);
}
