package com.cashly.cashly_api.warehouses.reporting.infrastructure.config;

import com.cashly.cashly_api.warehouses.reporting.application.ports.out.MovementsExcelPort;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.MovementsPdfPort;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.MovementsReportRepositoryPort;
import com.cashly.cashly_api.warehouses.reporting.application.generate_movements_excel.use_case.GenerateMovementsExcelUseCase;
import com.cashly.cashly_api.warehouses.reporting.application.generate_movements_excel.services.GenerateMovementsExcelUseCaseImpl;
import com.cashly.cashly_api.warehouses.reporting.application.generate_movements_pdf.use_case.GenerateMovementsPdfUseCase;
import com.cashly.cashly_api.warehouses.reporting.application.generate_movements_pdf.services.GenerateMovementsPdfUseCaseImpl;
import com.cashly.cashly_api.warehouses.reporting.infrastructure.excel.MovementsExcelGenerator;
import com.cashly.cashly_api.warehouses.reporting.infrastructure.pdf.MovementsPdfGenerator;
import com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.repositories.MovementsReportJpaRepository;
import com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.adapters.MovementsReportRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MovementsReportConfig {

    @Bean
    public MovementsReportRepositoryPort movementsReportRepositoryPort(
            MovementsReportJpaRepository movementsReportJpaRepository) {
        return new MovementsReportRepositoryAdapter(movementsReportJpaRepository);
    }

    @Bean
    public MovementsPdfPort movementsPdfPort() {
        return new MovementsPdfGenerator();
    }

    @Bean
    public MovementsExcelPort movementsExcelPort() {
        return new MovementsExcelGenerator();
    }

    @Bean
    public GenerateMovementsPdfUseCase generateMovementsPdfUseCase(
            MovementsReportRepositoryPort movementsReportRepositoryPort,
            MovementsPdfPort movementsPdfPort) {
        return new GenerateMovementsPdfUseCaseImpl(movementsReportRepositoryPort, movementsPdfPort);
    }

    @Bean
    public GenerateMovementsExcelUseCase generateMovementsExcelUseCase(
            MovementsReportRepositoryPort movementsReportRepositoryPort,
            MovementsExcelPort movementsExcelPort) {
        return new GenerateMovementsExcelUseCaseImpl(movementsReportRepositoryPort, movementsExcelPort);
    }
}
