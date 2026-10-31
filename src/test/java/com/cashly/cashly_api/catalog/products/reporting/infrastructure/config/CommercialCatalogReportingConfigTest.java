package com.cashly.cashly_api.catalog.products.reporting.infrastructure.config;

import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.cashly.cashly_api.catalog.products.reporting.application.ports.out.CommercialCatalogPdfPort;
import com.cashly.cashly_api.catalog.products.reporting.application.ports.out.CommercialCatalogRepositoryPort;
import com.cashly.cashly_api.catalog.products.reporting.application.ports.out.MediaGateway;
import com.cashly.cashly_api.catalog.products.reporting.infrastructure.persistence.repositories.CommercialCatalogJpaRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class CommercialCatalogReportingConfigTest {
    @Test
    void should_create_commercial_catalog_beans() {
        var config = new CommercialCatalogReportingConfig();
        assertNotNull(config.commercialCatalogRepositoryPort(
                mock(CommercialCatalogJpaRepository.class)));
        assertNotNull(config.commercialCatalogPdfPort(mock(MediaGateway.class),
                new PdfBranding()));
        assertNotNull(config.generateCommercialCatalogPdfUseCase(
                mock(CommercialCatalogRepositoryPort.class),
                mock(CommercialCatalogPdfPort.class)));
    }
}
