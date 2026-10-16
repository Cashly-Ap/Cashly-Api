package com.cashly.cashly_api.warehouses.reporting.infrastructure.config;

import com.cashly.cashly_api.warehouses.reporting.application.ports.out.MovementsExcelPort;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.MovementsPdfPort;
import com.cashly.cashly_api.warehouses.reporting.application.ports.out.MovementsReportRepositoryPort;
import com.cashly.cashly_api.warehouses.reporting.application.generate_movements_excel.GenerateMovementsExcelUseCase;
import com.cashly.cashly_api.warehouses.reporting.application.generate_movements_pdf.GenerateMovementsPdfUseCase;
import com.cashly.cashly_api.warehouses.reporting.infrastructure.persistence.repositories.MovementsReportJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovementsReportConfig Unit Tests")
class MovementsReportConfigTest {

    @Mock
    private MovementsReportJpaRepository movementsReportJpaRepository;

    @Mock
    private MovementsReportRepositoryPort movementsReportRepositoryPort;

    @Mock
    private MovementsPdfPort movementsPdfPort;

    @Mock
    private MovementsExcelPort movementsExcelPort;

    @Test
    @DisplayName("should create use case beans")
    void should_create_use_case_beans() {
        var config = new MovementsReportConfig();

        var pdfUseCase = config.generateMovementsPdfUseCase(movementsReportRepositoryPort, movementsPdfPort);
        assertNotNull(pdfUseCase);

        var excelUseCase = config.generateMovementsExcelUseCase(movementsReportRepositoryPort, movementsExcelPort);
        assertNotNull(excelUseCase);
    }

    @Test
    @DisplayName("should create port and adapter beans")
    void should_create_port_and_adapter_beans() {
        var config = new MovementsReportConfig();

        assertNotNull(config.movementsReportRepositoryPort(movementsReportJpaRepository));
        assertNotNull(config.movementsPdfPort());
        assertNotNull(config.movementsExcelPort());
    }
}
