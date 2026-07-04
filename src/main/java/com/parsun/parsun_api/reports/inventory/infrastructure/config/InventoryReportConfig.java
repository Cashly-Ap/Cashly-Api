package com.parsun.parsun_api.reports.inventory.infrastructure.config;

import com.parsun.parsun_api.reports.inventory.application.port.CatalogPdfPort;
import com.parsun.parsun_api.reports.inventory.application.port.CatalogReportRepositoryPort;
import com.parsun.parsun_api.reports.inventory.application.port.CommercialCatalogPdfPort;
import com.parsun.parsun_api.reports.inventory.application.port.CommercialCatalogRepositoryPort;
import com.parsun.parsun_api.reports.inventory.application.usecases.GenerateCatalogPdfUseCase;
import com.parsun.parsun_api.reports.inventory.application.usecases.GenerateCatalogPdfUseCaseImpl;
import com.parsun.parsun_api.reports.inventory.application.usecases.GenerateCommercialCatalogPdfUseCase;
import com.parsun.parsun_api.reports.inventory.application.usecases.GenerateCommercialCatalogPdfUseCaseImpl;
import com.parsun.parsun_api.reports.inventory.application.usecases.GetCatalogReportUseCase;
import com.parsun.parsun_api.reports.inventory.application.usecases.GetCatalogReportUseCaseImpl;
import com.parsun.parsun_api.reports.inventory.infrastructure.pdf.CatalogPdfGenerator;
import com.parsun.parsun_api.reports.inventory.infrastructure.pdf.CommercialCatalogPdfGenerator;
import com.parsun.parsun_api.reports.inventory.infrastructure.persistence.CatalogReportJpaRepository;
import com.parsun.parsun_api.reports.inventory.infrastructure.persistence.CatalogReportRepositoryAdapter;
import com.parsun.parsun_api.reports.inventory.infrastructure.persistence.CommercialCatalogJpaRepository;
import com.parsun.parsun_api.reports.inventory.infrastructure.persistence.CommercialCatalogRepositoryAdapter;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
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
    public CommercialCatalogPdfPort commercialCatalogPdfPort() {
        return new CommercialCatalogPdfGenerator();
    }

    @Bean
    public GenerateCommercialCatalogPdfUseCase generateCommercialCatalogPdfUseCase(
            CommercialCatalogRepositoryPort commercialCatalogRepositoryPort,
            CommercialCatalogPdfPort commercialCatalogPdfPort,
            FileStorageService fileStorageService) {
        return new GenerateCommercialCatalogPdfUseCaseImpl(
                commercialCatalogRepositoryPort, commercialCatalogPdfPort, fileStorageService);
    }
}
