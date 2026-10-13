package com.cashly.cashly_api.reports.inventory.application.usecases;

import com.cashly.cashly_api.reports.inventory.application.dto.query.CommercialCatalogQuery;

public interface GenerateCommercialCatalogPdfUseCase {
    byte[] execute(CommercialCatalogQuery query);
}
