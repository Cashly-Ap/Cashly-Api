package com.cashly.cashly_api.warehouses.reporting.application.get_catalog_report;

import com.cashly.cashly_api.warehouses.reporting.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.warehouses.reporting.application.dto.query.CatalogReportQuery;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.CatalogReportRepositoryPort;
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
