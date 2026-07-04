package com.parsun.parsun_api.reports.inventory.application.usecases;

import com.parsun.parsun_api.reports.inventory.application.dto.response.CommercialCatalogResponse;
import com.parsun.parsun_api.reports.inventory.application.port.CommercialCatalogPdfPort;
import com.parsun.parsun_api.reports.inventory.application.port.CommercialCatalogRepositoryPort;
import com.parsun.parsun_api.reports.inventory.application.query.CommercialCatalogQuery;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;

import java.util.List;

public class GenerateCommercialCatalogPdfUseCaseImpl implements GenerateCommercialCatalogPdfUseCase {

    private final CommercialCatalogRepositoryPort repositoryPort;
    private final CommercialCatalogPdfPort pdfPort;
    private final FileStorageService fileStorageService;

    public GenerateCommercialCatalogPdfUseCaseImpl(
            CommercialCatalogRepositoryPort repositoryPort,
            CommercialCatalogPdfPort pdfPort,
            FileStorageService fileStorageService) {
        this.repositoryPort = repositoryPort;
        this.pdfPort = pdfPort;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public byte[] execute(CommercialCatalogQuery query) {
        List<CommercialCatalogResponse> items = repositoryPort.findCatalog(query);
        return pdfPort.generatePdf(items, fileStorageService);
    }
}
