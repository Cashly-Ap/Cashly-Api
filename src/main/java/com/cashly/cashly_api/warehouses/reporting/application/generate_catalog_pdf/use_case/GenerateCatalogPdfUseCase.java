package com.cashly.cashly_api.warehouses.reporting.application.generate_catalog_pdf.use_case;

import com.cashly.cashly_api.warehouses.reporting.application.dto.query.model.CatalogPdfQuery;

public interface GenerateCatalogPdfUseCase {
    byte[] execute(CatalogPdfQuery query);
}
