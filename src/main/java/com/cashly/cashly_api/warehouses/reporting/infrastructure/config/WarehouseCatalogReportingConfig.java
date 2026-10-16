package com.cashly.cashly_api.warehouses.reporting.infrastructure.config;

import com.cashly.cashly_api.warehouses.reporting.application.ports.out.CatalogPdfPort;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.CatalogReportRepositoryPort;
import com.cashly.cashly_api.warehouses.reporting.application.generate_catalog_pdf.use_case.GenerateCatalogPdfUseCase;
import com.cashly.cashly_api.warehouses.reporting.application.generate_catalog_pdf.services.GenerateCatalogPdfUseCaseImpl;
import com.cashly.cashly_api.warehouses.reporting.application.get_catalog_report.use_case.GetCatalogReportUseCase;
import com.cashly.cashly_api.warehouses.reporting.application.get_catalog_report.services.GetCatalogReportUseCaseImpl;
import com.cashly.cashly_api.warehouses.reporting.infrastructure.pdf.CatalogPdfGenerator;
import com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.repositories.CatalogReportJpaRepository;
import com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.adapters.CatalogReportRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WarehouseCatalogReportingConfig {

    @Bean
    public CatalogReportRepositoryPort catalogReportRepositoryPort(
            CatalogReportJpaRepository catalogReportJpaRepository) {
        return new CatalogReportRepositoryAdapter(catalogReportJpaRepository);
    }

    @Bean
    public GetCatalogReportUseCase getCatalogReportUseCase(
            CatalogReportRepositoryPort catalogReportRepositoryPort) {
        return new GetCatalogReportUseCaseImpl(catalogReportRepositoryPort);
    }

    @Bean
    public CatalogPdfPort catalogPdfPort() {
        return new CatalogPdfGenerator();
    }

    @Bean
    public GenerateCatalogPdfUseCase generateCatalogPdfUseCase(
            CatalogReportRepositoryPort catalogReportRepositoryPort,
            CatalogPdfPort catalogPdfPort) {
        return new GenerateCatalogPdfUseCaseImpl(catalogReportRepositoryPort, catalogPdfPort);
    }
}
