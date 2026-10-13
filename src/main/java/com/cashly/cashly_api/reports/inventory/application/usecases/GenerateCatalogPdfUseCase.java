package com.cashly.cashly_api.reports.inventory.application.usecases;

import com.cashly.cashly_api.reports.inventory.application.dto.query.CatalogPdfQuery;

public interface GenerateCatalogPdfUseCase {
    byte[] execute(CatalogPdfQuery query);
}
