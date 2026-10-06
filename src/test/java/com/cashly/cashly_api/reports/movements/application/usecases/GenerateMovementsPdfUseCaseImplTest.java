package com.cashly.cashly_api.reports.movements.application.usecases;

import com.cashly.cashly_api.reports.movements.application.dto.response.MovementReportResponse;
import com.cashly.cashly_api.reports.movements.application.port.MovementsPdfPort;
import com.cashly.cashly_api.reports.movements.application.port.MovementsReportRepositoryPort;
import com.cashly.cashly_api.reports.movements.application.query.MovementsPdfQuery;
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
@DisplayName("GenerateMovementsPdfUseCaseImpl Unit Tests")
class GenerateMovementsPdfUseCaseImplTest {

    @Mock
    private MovementsReportRepositoryPort repository;

    @Mock
    private MovementsPdfPort pdfPort;

    @InjectMocks
    private GenerateMovementsPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate movements PDF from query")
    void should_generate_movements_pdf() {
        MovementsPdfQuery query = new MovementsPdfQuery(
                UUID.randomUUID(), UUID.randomUUID(), "EXIT",
                java.time.LocalDate.of(2026, 1, 1), java.time.LocalDate.of(2026, 12, 31));
        List<MovementReportResponse> items = List.of(
                new MovementReportResponse(UUID.randomUUID(), "Warehouse", "Product",
                        "SKU001", "Salida", 5, 10, 5, "exit notes", LocalDateTime.now()));
        byte[] expectedPdf = "Movements PDF".getBytes();

        when(repository.findAllForPdf(query)).thenReturn(items);
        when(pdfPort.generatePdf(items)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedPdf, result);
        verify(repository).findAllForPdf(query);
        verify(pdfPort).generatePdf(items);
    }

    @Test
    @DisplayName("should generate movements PDF with empty result")
    void should_generate_movements_pdf_with_empty_result() {
        MovementsPdfQuery query = new MovementsPdfQuery(
                null, null, null, null, null);
        List<MovementReportResponse> items = List.of();
        byte[] expectedPdf = "Empty PDF".getBytes();

        when(repository.findAllForPdf(query)).thenReturn(items);
        when(pdfPort.generatePdf(items)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedPdf, result);
        verify(repository).findAllForPdf(query);
        verify(pdfPort).generatePdf(items);
    }
}
