package com.cashly.cashly_api.reports.movements.infrastructure.config;

import com.cashly.cashly_api.reports.movements.application.port.MovementsExcelPort;
import com.cashly.cashly_api.reports.movements.application.port.MovementsPdfPort;
import com.cashly.cashly_api.reports.movements.application.port.MovementsReportRepositoryPort;
import com.cashly.cashly_api.reports.movements.application.usecases.GenerateMovementsExcelUseCase;
import com.cashly.cashly_api.reports.movements.application.usecases.GenerateMovementsExcelUseCaseImpl;
import com.cashly.cashly_api.reports.movements.application.usecases.GenerateMovementsPdfUseCase;
import com.cashly.cashly_api.reports.movements.application.usecases.GenerateMovementsPdfUseCaseImpl;
import com.cashly.cashly_api.reports.movements.infrastructure.excel.MovementsExcelGenerator;
import com.cashly.cashly_api.reports.movements.infrastructure.pdf.MovementsPdfGenerator;
import com.cashly.cashly_api.reports.movements.infrastructure.persistence.MovementsReportJpaRepository;
import com.cashly.cashly_api.reports.movements.infrastructure.persistence.MovementsReportRepositoryAdapter;
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
