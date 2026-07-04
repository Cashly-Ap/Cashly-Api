package com.parsun.parsun_api.reports.inventory.application.usecases;

import com.parsun.parsun_api.reports.inventory.application.query.CommercialCatalogQuery;

public interface GenerateCommercialCatalogPdfUseCase {
    byte[] execute(CommercialCatalogQuery query);
}
