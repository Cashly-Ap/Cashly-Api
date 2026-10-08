package com.cashly.cashly_api.reports.inventory.infrastructure.config;

import com.cashly.cashly_api.reports.inventory.application.port.CatalogPdfPort;
import com.cashly.cashly_api.reports.inventory.application.port.CatalogReportRepositoryPort;
import com.cashly.cashly_api.reports.inventory.application.port.CommercialCatalogPdfPort;
import com.cashly.cashly_api.reports.inventory.application.port.CommercialCatalogRepositoryPort;
import com.cashly.cashly_api.reports.inventory.application.usecases.GenerateCatalogPdfUseCase;
import com.cashly.cashly_api.reports.inventory.application.usecases.GenerateCatalogPdfUseCaseImpl;
import com.cashly.cashly_api.reports.inventory.application.usecases.GenerateCommercialCatalogPdfUseCase;
import com.cashly.cashly_api.reports.inventory.application.usecases.GenerateCommercialCatalogPdfUseCaseImpl;
import com.cashly.cashly_api.reports.inventory.application.usecases.GetCatalogReportUseCase;
import com.cashly.cashly_api.reports.inventory.application.usecases.GetCatalogReportUseCaseImpl;
import com.cashly.cashly_api.reports.inventory.infrastructure.pdf.CatalogPdfGenerator;
import com.cashly.cashly_api.reports.inventory.infrastructure.pdf.CommercialCatalogPdfGenerator;
import com.cashly.cashly_api.reports.inventory.infrastructure.persistence.CatalogReportJpaRepository;
import com.cashly.cashly_api.reports.inventory.infrastructure.persistence.CatalogReportRepositoryAdapter;
import com.cashly.cashly_api.reports.inventory.infrastructure.persistence.CommercialCatalogJpaRepository;
import com.cashly.cashly_api.reports.inventory.infrastructure.persistence.CommercialCatalogRepositoryAdapter;
import com.cashly.cashly_api.reports.inventory.application.contracts.MediaGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryReportConfig {

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

    @Bean
    public CommercialCatalogRepositoryPort commercialCatalogRepositoryPort(
            CommercialCatalogJpaRepository commercialCatalogJpaRepository) {
        return new CommercialCatalogRepositoryAdapter(commercialCatalogJpaRepository);
    }

    @Bean
    public CommercialCatalogPdfPort commercialCatalogPdfPort(MediaGateway media) {
        return new CommercialCatalogPdfGenerator(media);
    }

    @Bean
    public GenerateCommercialCatalogPdfUseCase generateCommercialCatalogPdfUseCase(
            CommercialCatalogRepositoryPort commercialCatalogRepositoryPort,
            CommercialCatalogPdfPort commercialCatalogPdfPort) {
        return new GenerateCommercialCatalogPdfUseCaseImpl(
                commercialCatalogRepositoryPort, commercialCatalogPdfPort);
    }
}
