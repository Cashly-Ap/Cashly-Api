package com.cashly.cashly_api.catalog.products.reporting.application.generate_commercial_catalog_pdf.services;

import com.cashly.cashly_api.catalog.products.reporting.application.generate_commercial_catalog_pdf.use_case.GenerateCommercialCatalogPdfUseCase;

import com.cashly.cashly_api.catalog.products.reporting.application.dto.response.CommercialCatalogResponse;
import com.cashly.cashly_api.catalog.products.reporting.application.ports.out.CommercialCatalogPdfPort;
import com.cashly.cashly_api.catalog.products.reporting.application.ports.out.CommercialCatalogRepositoryPort;
import com.cashly.cashly_api.catalog.products.reporting.application.dto.query.model.CommercialCatalogQuery;

import java.util.List;

public class GenerateCommercialCatalogPdfUseCaseImpl implements GenerateCommercialCatalogPdfUseCase {

    private final CommercialCatalogRepositoryPort repositoryPort;
    private final CommercialCatalogPdfPort pdfPort;


    public GenerateCommercialCatalogPdfUseCaseImpl(
            CommercialCatalogRepositoryPort repositoryPort,
            CommercialCatalogPdfPort pdfPort) {
        this.repositoryPort = repositoryPort;
        this.pdfPort = pdfPort;

    }

    @Override
    public byte[] execute(CommercialCatalogQuery query) {
        List<CommercialCatalogResponse> items = repositoryPort.findCatalog(query);
        return pdfPort.generatePdf(items);
    }
}
