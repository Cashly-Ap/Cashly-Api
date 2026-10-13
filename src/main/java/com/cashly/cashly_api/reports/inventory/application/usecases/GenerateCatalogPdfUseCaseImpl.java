package com.cashly.cashly_api.reports.inventory.application.usecases;

import com.cashly.cashly_api.reports.inventory.application.ports.out.CatalogPdfPort;
import com.cashly.cashly_api.reports.inventory.application.ports.out.CatalogReportRepositoryPort;
import com.cashly.cashly_api.reports.inventory.application.dto.query.CatalogPdfQuery;
import com.cashly.cashly_api.reports.inventory.application.dto.query.CatalogReportQuery;

public class GenerateCatalogPdfUseCaseImpl implements GenerateCatalogPdfUseCase {

    private final CatalogReportRepositoryPort catalogReportRepositoryPort;
    private final CatalogPdfPort catalogPdfPort;

    public GenerateCatalogPdfUseCaseImpl(
            CatalogReportRepositoryPort catalogReportRepositoryPort,
            CatalogPdfPort catalogPdfPort) {
        this.catalogReportRepositoryPort = catalogReportRepositoryPort;
        this.catalogPdfPort = catalogPdfPort;
    }

    @Override
    public byte[] execute(CatalogPdfQuery query) {
        CatalogReportQuery reportQuery = new CatalogReportQuery(
                query.search(),
                query.categoryId(),
                query.warehouseId(),
                query.composite(),
                query.withStock(),
                query.lowStock(),
                0,
                10000
        );
        var items = catalogReportRepositoryPort.findCatalog(reportQuery).content();
        return catalogPdfPort.generatePdf(items);
    }
}
