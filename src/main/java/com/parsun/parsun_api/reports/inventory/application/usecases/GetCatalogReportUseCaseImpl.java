package com.parsun.parsun_api.reports.inventory.application.usecases;

import com.parsun.parsun_api.reports.inventory.application.dto.response.CatalogReportResponse;
import com.parsun.parsun_api.reports.inventory.application.query.CatalogReportQuery;
import com.parsun.parsun_api.reports.inventory.application.port.CatalogReportRepositoryPort;
import com.parsun.parsun_api.shared.dto.PageResponse;

public class GetCatalogReportUseCaseImpl implements GetCatalogReportUseCase {

    private final CatalogReportRepositoryPort catalogReportRepositoryPort;

    public GetCatalogReportUseCaseImpl(CatalogReportRepositoryPort catalogReportRepositoryPort) {
        this.catalogReportRepositoryPort = catalogReportRepositoryPort;
    }

    @Override
    public PageResponse<CatalogReportResponse> execute(CatalogReportQuery query) {
        return catalogReportRepositoryPort.findCatalog(query);
    }
}
