package com.cashly.cashly_api.products.reporting.application.generate_commercial_catalog_pdf;

import com.cashly.cashly_api.products.reporting.application.dto.query.CommercialCatalogQuery;

public interface GenerateCommercialCatalogPdfUseCase {
    byte[] execute(CommercialCatalogQuery query);
}
