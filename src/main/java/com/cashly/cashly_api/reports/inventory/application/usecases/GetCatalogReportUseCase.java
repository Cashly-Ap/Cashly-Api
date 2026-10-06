package com.cashly.cashly_api.reports.inventory.application.usecases;

import com.cashly.cashly_api.reports.inventory.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.reports.inventory.application.query.CatalogReportQuery;
import com.cashly.cashly_api.shared.dto.PageResponse;

public interface GetCatalogReportUseCase {
    PageResponse<CatalogReportResponse> execute(CatalogReportQuery query);
}
