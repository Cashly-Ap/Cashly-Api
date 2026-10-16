package com.cashly.cashly_api.warehouses.reporting.application.generate_catalog_pdf;

import com.cashly.cashly_api.warehouses.reporting.application.dto.query.CatalogPdfQuery;

public interface GenerateCatalogPdfUseCase {
    byte[] execute(CatalogPdfQuery query);
}
