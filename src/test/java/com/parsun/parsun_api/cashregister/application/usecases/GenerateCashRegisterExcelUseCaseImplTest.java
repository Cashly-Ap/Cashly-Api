package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterExportRow;
import com.parsun.parsun_api.cashregister.application.port.CashRegisterExcelPort;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateCashRegisterExcelUseCaseImpl Unit Tests")
class GenerateCashRegisterExcelUseCaseImplTest {

    @Mock
    private CashRegisterRepository repository;

    @Mock
    private CashRegisterExcelPort excelPort;

    private GenerateCashRegisterExcelUseCaseImpl useCase;

    private static final LocalDate DATE_FROM = LocalDate.of(2025, 6, 15);
    private static final LocalDate DATE_TO = LocalDate.of(2025, 6, 15);

    @BeforeEach
    void setUp() {
        useCase = new GenerateCashRegisterExcelUseCaseImpl(repository, excelPort);
    }

    @Test
    @DisplayName("should generate Excel by fetching rows and delegating to port")
    void shouldGenerateExcelByFetchingRowsAndDelegatingToPort() {
        List<CashRegisterExportRow> mockRows = List.of(
                CashRegisterExportRow.of(DATE_FROM, "FAC", "INV-001", BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(500), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Cliente A",
                        "VENTAS", 1)
        );
        byte[] expectedBytes = new byte[]{1, 2, 3};

        when(repository.findExportRows(DATE_FROM, DATE_TO)).thenReturn(mockRows);
        when(excelPort.generateExcel(mockRows)).thenReturn(expectedBytes);

        byte[] result = useCase.execute(DATE_FROM, DATE_TO);

        assertNotNull(result);
        assertArrayEquals(expectedBytes, result);
        verify(repository).findExportRows(DATE_FROM, DATE_TO);
        verify(excelPort).generateExcel(mockRows);
    }

    @Test
    @DisplayName("should generate Excel with empty rows when no data")
    void shouldGenerateExcelWithEmptyRowsWhenNoData() {
        when(repository.findExportRows(DATE_FROM, DATE_TO)).thenReturn(List.of());
        when(excelPort.generateExcel(List.of())).thenReturn(new byte[0]);

        byte[] result = useCase.execute(DATE_FROM, DATE_TO);

        assertNotNull(result);
        assertEquals(0, result.length);
        verify(repository).findExportRows(DATE_FROM, DATE_TO);
        verify(excelPort).generateExcel(List.of());
    }

    @Test
    @DisplayName("should propagate exception when repository fails")
    void shouldPropagateExceptionWhenRepositoryFails() {
        when(repository.findExportRows(DATE_FROM, DATE_TO)).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> useCase.execute(DATE_FROM, DATE_TO));
        verify(repository).findExportRows(DATE_FROM, DATE_TO);
        verifyNoInteractions(excelPort);
    }
}
