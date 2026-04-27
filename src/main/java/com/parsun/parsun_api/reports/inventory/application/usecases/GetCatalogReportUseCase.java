package com.parsun.parsun_api.reports.inventory.application.usecases;

import com.parsun.parsun_api.reports.inventory.application.dto.response.CatalogReportResponse;
import com.parsun.parsun_api.reports.inventory.application.query.CatalogReportQuery;
import com.parsun.parsun_api.shared.dto.PageResponse;

public interface GetCatalogReportUseCase {
    PageResponse<CatalogReportResponse> execute(CatalogReportQuery query);
}
