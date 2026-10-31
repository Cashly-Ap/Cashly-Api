package com.cashly.cashly_api.inventory.warehouses.reporting.application.get_catalog_report.services;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.get_catalog_report.use_case.GetCatalogReportUseCase;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.CatalogReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.CatalogReportQuery;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.CatalogReportRepositoryPort;
import com.cashly.cashly_api.shared.dto.PageResponse;

public class GetCatalogReportUseCaseImpl implements GetCatalogReportUseCase {

    private final CatalogReportRepositoryPort catalogReportRepositoryPort;

    public GetCatalogReportUseCaseImpl(
            CatalogReportRepositoryPort catalogReportRepositoryPort) {
        this.catalogReportRepositoryPort = catalogReportRepositoryPort;
    }

    @Override
    public PageResponse<CatalogReportResponse> execute(
            CatalogReportQuery query) {
        return catalogReportRepositoryPort.findCatalog(query);
    }
}
