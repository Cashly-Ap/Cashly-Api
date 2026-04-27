package com.parsun.parsun_api.reports.inventory.application.usecases;

import com.parsun.parsun_api.reports.inventory.application.query.CatalogPdfQuery;

public interface GenerateCatalogPdfUseCase {
    byte[] execute(CatalogPdfQuery query);
}
