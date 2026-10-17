package com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_catalog_pdf.use_case;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.CatalogPdfQuery;

public interface GenerateCatalogPdfUseCase {
    byte[] execute(CatalogPdfQuery query);
}
