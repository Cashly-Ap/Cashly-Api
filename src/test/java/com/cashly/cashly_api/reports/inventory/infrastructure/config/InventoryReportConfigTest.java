package com.cashly.cashly_api.reports.inventory.infrastructure.config;

import com.cashly.cashly_api.reports.inventory.application.ports.out.CatalogPdfPort;
import com.cashly.cashly_api.reports.inventory.application.ports.out.CatalogReportRepositoryPort;
import com.cashly.cashly_api.reports.inventory.application.usecases.GenerateCatalogPdfUseCase;
import com.cashly.cashly_api.reports.inventory.application.usecases.GetCatalogReportUseCase;
import com.cashly.cashly_api.reports.inventory.infrastructure.adapters.out.persistence.repositories.CatalogReportJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryReportConfig Unit Tests")
class InventoryReportConfigTest {

    @Mock
    private CatalogReportJpaRepository catalogReportJpaRepository;

    @Mock
    private CatalogReportRepositoryPort catalogReportRepositoryPort;

    @Mock
    private CatalogPdfPort catalogPdfPort;

    @Test
    @DisplayName("should create use case beans")
    void should_create_use_case_beans() {
        var config = new InventoryReportConfig();

        var getCatalogUseCase = config.getCatalogReportUseCase(catalogReportRepositoryPort);
        assertNotNull(getCatalogUseCase);

        var generatePdfUseCase = config.generateCatalogPdfUseCase(catalogReportRepositoryPort, catalogPdfPort);
        assertNotNull(generatePdfUseCase);
    }

    @Test
    @DisplayName("should create port and adapter beans")
    void should_create_port_and_adapter_beans() {
        var config = new InventoryReportConfig();

        assertNotNull(config.catalogReportRepositoryPort(catalogReportJpaRepository));
        assertNotNull(config.catalogPdfPort());
    }
}
