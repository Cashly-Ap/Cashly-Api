package com.cashly.cashly_api.products.reporting.infrastructure.config;

import com.cashly.cashly_api.products.reporting.application.ports.out.CommercialCatalogPdfPort;
import com.cashly.cashly_api.products.reporting.application.ports.out.CommercialCatalogRepositoryPort;
import com.cashly.cashly_api.products.reporting.application.generate_commercial_catalog_pdf.GenerateCommercialCatalogPdfUseCase;
import com.cashly.cashly_api.products.reporting.application.generate_commercial_catalog_pdf.GenerateCommercialCatalogPdfUseCaseImpl;
import com.cashly.cashly_api.products.reporting.infrastructure.pdf.CommercialCatalogPdfGenerator;
import com.cashly.cashly_api.products.reporting.infrastructure.persistence.repositories.CommercialCatalogJpaRepository;
import com.cashly.cashly_api.products.reporting.infrastructure.persistence.adapters.CommercialCatalogRepositoryAdapter;
import com.cashly.cashly_api.products.reporting.application.ports.out.MediaGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommercialCatalogReportingConfig {

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
