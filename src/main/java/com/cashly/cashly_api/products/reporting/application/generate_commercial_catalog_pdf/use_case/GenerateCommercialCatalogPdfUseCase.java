package com.cashly.cashly_api.products.reporting.application.generate_commercial_catalog_pdf.use_case;

import com.cashly.cashly_api.products.reporting.application.dto.query.model.CommercialCatalogQuery;

public interface GenerateCommercialCatalogPdfUseCase {
    byte[] execute(CommercialCatalogQuery query);
}
