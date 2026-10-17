package com.cashly.cashly_api.catalog.products.reporting.application.ports.out;

import com.cashly.cashly_api.catalog.products.reporting.application.dto.response.CommercialCatalogResponse;

import java.util.List;

public interface CommercialCatalogPdfPort {
    byte[] generatePdf(List<CommercialCatalogResponse> items);
}
