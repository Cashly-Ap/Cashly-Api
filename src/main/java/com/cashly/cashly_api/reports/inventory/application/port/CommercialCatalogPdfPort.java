package com.cashly.cashly_api.reports.inventory.application.port;

import com.cashly.cashly_api.reports.inventory.application.dto.response.CommercialCatalogResponse;

import java.util.List;

public interface CommercialCatalogPdfPort {
    byte[] generatePdf(List<CommercialCatalogResponse> items);
}
