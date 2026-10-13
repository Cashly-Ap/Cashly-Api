package com.cashly.cashly_api.reports.inventory.infrastructure.config;

import com.cashly.cashly_api.reports.inventory.application.ports.out.CatalogPdfPort;
import com.cashly.cashly_api.reports.inventory.application.ports.out.CatalogReportRepositoryPort;
import com.cashly.cashly_api.reports.inventory.application.ports.out.CommercialCatalogPdfPort;
import com.cashly.cashly_api.reports.inventory.application.ports.out.CommercialCatalogRepositoryPort;
import com.cashly.cashly_api.reports.inventory.application.usecases.GenerateCatalogPdfUseCase;
import com.cashly.cashly_api.reports.inventory.application.usecases.GenerateCatalogPdfUseCaseImpl;
import com.cashly.cashly_api.reports.inventory.application.usecases.GenerateCommercialCatalogPdfUseCase;
import com.cashly.cashly_api.reports.inventory.application.usecases.GenerateCommercialCatalogPdfUseCaseImpl;
import com.cashly.cashly_api.reports.inventory.application.usecases.GetCatalogReportUseCase;
import com.cashly.cashly_api.reports.inventory.application.usecases.GetCatalogReportUseCaseImpl;
import com.cashly.cashly_api.reports.inventory.infrastructure.adapters.out.pdf.CatalogPdfGenerator;
import com.cashly.cashly_api.reports.inventory.infrastructure.adapters.out.pdf.CommercialCatalogPdfGenerator;
import com.cashly.cashly_api.reports.inventory.infrastructure.adapters.out.persistence.repositories.CatalogReportJpaRepository;
import com.cashly.cashly_api.reports.inventory.infrastructure.adapters.out.persistence.adapters.CatalogReportRepositoryAdapter;
import com.cashly.cashly_api.reports.inventory.infrastructure.adapters.out.persistence.repositories.CommercialCatalogJpaRepository;
import com.cashly.cashly_api.reports.inventory.infrastructure.adapters.out.persistence.adapters.CommercialCatalogRepositoryAdapter;
import com.cashly.cashly_api.reports.inventory.application.ports.out.MediaGateway;
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
