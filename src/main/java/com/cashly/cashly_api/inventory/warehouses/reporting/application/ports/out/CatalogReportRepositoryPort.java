package com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.CatalogReportQuery;
import com.cashly.cashly_api.shared.dto.PageResponse;

public interface CatalogReportRepositoryPort {
    PageResponse<CatalogReportResponse> findCatalog(CatalogReportQuery query);
}
