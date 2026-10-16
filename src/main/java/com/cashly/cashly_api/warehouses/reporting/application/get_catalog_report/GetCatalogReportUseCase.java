package com.cashly.cashly_api.warehouses.reporting.application.get_catalog_report;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.CatalogReportQuery;
import com.cashly.cashly_api.shared.dto.PageResponse;

public interface GetCatalogReportUseCase {
    PageResponse<CatalogReportResponse> execute(CatalogReportQuery query);
}
