package com.parsun.parsun_api.reports.movements.infrastructure.web;

import com.parsun.parsun_api.reports.movements.application.query.MovementsPdfQuery;
import com.parsun.parsun_api.reports.movements.application.usecases.GenerateMovementsExcelUseCase;
import com.parsun.parsun_api.reports.movements.application.usecases.GenerateMovementsPdfUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovementsReportController Unit Tests")
class MovementsReportControllerTest {

    @Mock
    private GenerateMovementsPdfUseCase generateMovementsPdfUseCase;

    @Mock
    private GenerateMovementsExcelUseCase generateMovementsExcelUseCase;

    @InjectMocks
    private MovementsReportController controller;

    @Test
    @DisplayName("should return movements PDF as attachment")
    void should_get_movements_pdf() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String movementType = "ENTRY";
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);
        byte[] pdfContent = "Movements PDF".getBytes();

        when(generateMovementsPdfUseCase.execute(any(MovementsPdfQuery.class)))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getMovementsPdf(
                warehouseId, productId, movementType, dateFrom, dateTo);

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("historial-movimientos.pdf"));
        assertArrayEquals(pdfContent, result.getBody());
        assertEquals(200, result.getStatusCodeValue());
        verify(generateMovementsPdfUseCase).execute(any(MovementsPdfQuery.class));
    }

    @Test
    @DisplayName("should return movements PDF without optional params")
    void should_get_movements_pdf_without_optional_params() {
        byte[] pdfContent = "PDF no filters".getBytes();

        when(generateMovementsPdfUseCase.execute(any(MovementsPdfQuery.class)))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getMovementsPdf(
                null, null, null, null, null);

        assertArrayEquals(pdfContent, result.getBody());
        verify(generateMovementsPdfUseCase).execute(any(MovementsPdfQuery.class));
    }

    @Test
    @DisplayName("should return movements Excel as attachment")
    void should_get_movements_excel() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        byte[] excelContent = "Excel content".getBytes();

        when(generateMovementsExcelUseCase.execute(any(MovementsPdfQuery.class)))
                .thenReturn(excelContent);

        ResponseEntity<byte[]> result = controller.getMovementsExcel(
                warehouseId, productId, "EXIT",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("historial-movimientos.xlsx"));
        assertArrayEquals(excelContent, result.getBody());
        verify(generateMovementsExcelUseCase).execute(any(MovementsPdfQuery.class));
    }
}
