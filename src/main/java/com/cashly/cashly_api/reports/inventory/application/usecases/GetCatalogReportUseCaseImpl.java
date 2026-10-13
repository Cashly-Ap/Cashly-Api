package com.cashly.cashly_api.reports.inventory.application.usecases;

import com.cashly.cashly_api.reports.inventory.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.reports.inventory.application.dto.query.CatalogReportQuery;
import com.cashly.cashly_api.reports.inventory.application.ports.out.CatalogReportRepositoryPort;
import com.cashly.cashly_api.shared.dto.PageResponse;

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
