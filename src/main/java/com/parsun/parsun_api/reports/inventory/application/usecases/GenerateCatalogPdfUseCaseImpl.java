package com.parsun.parsun_api.reports.inventory.application.usecases;

import com.parsun.parsun_api.reports.inventory.application.port.CatalogPdfPort;
import com.parsun.parsun_api.reports.inventory.application.port.CatalogReportRepositoryPort;
import com.parsun.parsun_api.reports.inventory.application.query.CatalogPdfQuery;
import com.parsun.parsun_api.reports.inventory.application.query.CatalogReportQuery;

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
