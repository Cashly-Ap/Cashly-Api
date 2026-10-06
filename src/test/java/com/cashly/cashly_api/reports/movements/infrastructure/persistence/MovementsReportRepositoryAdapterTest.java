package com.cashly.cashly_api.reports.movements.infrastructure.persistence;

import com.cashly.cashly_api.reports.movements.application.dto.response.MovementReportResponse;
import com.cashly.cashly_api.reports.movements.application.query.MovementsPdfQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovementsReportRepositoryAdapter Unit Tests")
class MovementsReportRepositoryAdapterTest {

    @Mock
    private MovementsReportJpaRepository jpaRepository;

    @InjectMocks
    private MovementsReportRepositoryAdapter adapter;

    @Test
    @DisplayName("should find movements and map to response with type labels")
    void should_find_movements() {
        MovementsPdfQuery query = new MovementsPdfQuery(
                UUID.randomUUID(), UUID.randomUUID(), "ENTRY",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        MovementsReportProjection projection = mock(MovementsReportProjection.class);
        when(projection.getMovementId()).thenReturn(UUID.randomUUID().toString());
        when(projection.getWarehouseName()).thenReturn("Main Warehouse");
        when(projection.getProductName()).thenReturn("Product A");
        when(projection.getSku()).thenReturn("SKU001");
        when(projection.getMovementType()).thenReturn("ENTRY");
        when(projection.getQuantity()).thenReturn(10);
        when(projection.getPreviousStock()).thenReturn(5);
        when(projection.getNewStock()).thenReturn(15);
        when(projection.getNotes()).thenReturn("Initial stock");
        when(projection.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(jpaRepository.findAllForPdf(any(), any(), any(), any(), any()))
                .thenReturn(List.of(projection));

        List<MovementReportResponse> result = adapter.findAllForPdf(query);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Main Warehouse", result.get(0).warehouseName());
        assertEquals("Product A", result.get(0).productName());
        assertEquals("Entrada", result.get(0).movementType());
        assertEquals(10, result.get(0).quantity());
        assertEquals(15, result.get(0).newStock());
        verify(jpaRepository).findAllForPdf(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("should map different movement types to labels")
    void should_map_movement_types() {
        MovementsPdfQuery query = new MovementsPdfQuery(
                null, null, null, null, null);

        MovementsReportProjection entry = mock(MovementsReportProjection.class);
        when(entry.getMovementId()).thenReturn(UUID.randomUUID().toString());
        when(entry.getMovementType()).thenReturn("ENTRY");
        when(entry.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(entry.getWarehouseName()).thenReturn("WH");
        when(entry.getProductName()).thenReturn("P");
        when(entry.getSku()).thenReturn("SKU");

        MovementsReportProjection exit = mock(MovementsReportProjection.class);
        when(exit.getMovementId()).thenReturn(UUID.randomUUID().toString());
        when(exit.getMovementType()).thenReturn("EXIT");
        when(exit.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(exit.getWarehouseName()).thenReturn("WH");
        when(exit.getProductName()).thenReturn("P");
        when(exit.getSku()).thenReturn("SKU");

        MovementsReportProjection transferIn = mock(MovementsReportProjection.class);
        when(transferIn.getMovementId()).thenReturn(UUID.randomUUID().toString());
        when(transferIn.getMovementType()).thenReturn("TRANSFER_IN");
        when(transferIn.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(transferIn.getWarehouseName()).thenReturn("WH");
        when(transferIn.getProductName()).thenReturn("P");
        when(transferIn.getSku()).thenReturn("SKU");

        when(jpaRepository.findAllForPdf(any(), any(), any(), any(), any()))
                .thenReturn(List.of(entry, exit, transferIn));

        List<MovementReportResponse> result = adapter.findAllForPdf(query);

        assertEquals("Entrada", result.get(0).movementType());
        assertEquals("Salida", result.get(1).movementType());
        assertEquals("Transferencia Entrada", result.get(2).movementType());
    }
}
