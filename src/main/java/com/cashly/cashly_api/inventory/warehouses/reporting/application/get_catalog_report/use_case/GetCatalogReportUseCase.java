package com.cashly.cashly_api.inventory.warehouses.reporting.application.get_catalog_report.use_case;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.CatalogReportQuery;
import com.cashly.cashly_api.shared.dto.PageResponse;

public interface GetCatalogReportUseCase {
    PageResponse<CatalogReportResponse> execute(CatalogReportQuery query);
}
