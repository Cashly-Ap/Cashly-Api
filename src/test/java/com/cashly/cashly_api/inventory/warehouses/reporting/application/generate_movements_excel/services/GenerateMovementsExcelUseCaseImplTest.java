package com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_movements_excel.services;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.response.MovementReportResponse;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.MovementsExcelPort;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.ports.out.MovementsReportRepositoryPort;
import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.MovementsPdfQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateMovementsExcelUseCaseImpl Unit Tests")
class GenerateMovementsExcelUseCaseImplTest {

    @Mock
    private MovementsReportRepositoryPort repository;

    @Mock
    private MovementsExcelPort excelPort;

    @InjectMocks
    private GenerateMovementsExcelUseCaseImpl useCase;

    @Test
    @DisplayName("should generate movements Excel from query")
    void should_generate_movements_excel() {
        MovementsPdfQuery query = new MovementsPdfQuery(UUID.randomUUID(),
                UUID.randomUUID(), "ENTRY", java.time.LocalDate.of(2026, 1, 1),
                java.time.LocalDate.of(2026, 12, 31));
        List<MovementReportResponse> items = List.of(new MovementReportResponse(
                UUID.randomUUID(), "Warehouse", "Product", "SKU001", "Entrada",
                10, 5, 15, "notes", LocalDateTime.now()));
        byte[] expectedExcel = "Excel content".getBytes();

        when(repository.findAllForPdf(query)).thenReturn(items);
        when(excelPort.generateExcel(items)).thenReturn(expectedExcel);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedExcel, result);
        verify(repository).findAllForPdf(query);
        verify(excelPort).generateExcel(items);
    }

    @Test
    @DisplayName("should generate movements Excel with empty result")
    void should_generate_movements_excel_with_empty_result() {
        MovementsPdfQuery query = new MovementsPdfQuery(null, null, null, null,
                null);
        List<MovementReportResponse> items = List.of();
        byte[] expectedExcel = "Empty Excel".getBytes();

        when(repository.findAllForPdf(query)).thenReturn(items);
        when(excelPort.generateExcel(items)).thenReturn(expectedExcel);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedExcel, result);
        verify(repository).findAllForPdf(query);
        verify(excelPort).generateExcel(items);
    }
}
