package com.cashly.cashly_api.reports.inventory.application.ports.out;

import com.cashly.cashly_api.reports.inventory.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.reports.inventory.application.dto.query.CatalogReportQuery;
import com.cashly.cashly_api.shared.dto.PageResponse;

public interface CatalogReportRepositoryPort {
    PageResponse<CatalogReportResponse> findCatalog(CatalogReportQuery query);
}
