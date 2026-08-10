package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.portal.application.dto.response.PortalProductResponse;
import com.parsun.parsun_api.reports.inventory.application.dto.response.CommercialCatalogResponse;
import com.parsun.parsun_api.reports.inventory.application.port.CommercialCatalogPdfPort;
import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;

import java.util.List;
import java.util.UUID;

public class GeneratePortalCatalogPdfUseCase {

    private static final int ALL_PRODUCTS_SIZE = 10000;

    private final ListProductsUseCase listProductsUseCase;
    private final CommercialCatalogPdfPort commercialCatalogPdfPort;
    private final FileStorageService fileStorageService;

    public GeneratePortalCatalogPdfUseCase(
            ListProductsUseCase listProductsUseCase,
            CommercialCatalogPdfPort commercialCatalogPdfPort,
            FileStorageService fileStorageService) {
        this.listProductsUseCase = listProductsUseCase;
        this.commercialCatalogPdfPort = commercialCatalogPdfPort;
        this.fileStorageService = fileStorageService;
    }

    public byte[] execute(UUID customerId) {
        PageResponse<PortalProductResponse> page = listProductsUseCase.execute(
                customerId, null, null, 0, ALL_PRODUCTS_SIZE);

        List<CommercialCatalogResponse> items = page.content().stream()
                .map(this::toCatalogResponse)
                .toList();

        return commercialCatalogPdfPort.generatePdf(items, fileStorageService);
    }

    private CommercialCatalogResponse toCatalogResponse(PortalProductResponse product) {
        return new CommercialCatalogResponse(
                UUID.fromString(product.id()),
                product.name(),
                product.code(),
                product.description(),
                product.photo(),
                UUID.fromString(product.categoryId()),
                product.categoryName(),
                product.price(),
                null
        );
    }
}
