package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterExportRow;
import com.parsun.parsun_api.cashregister.application.dto.CashRegisterReportFilter;
import com.parsun.parsun_api.cashregister.application.dto.ReportFormat;
import com.parsun.parsun_api.cashregister.application.dto.ReportType;
import com.parsun.parsun_api.cashregister.application.port.CashRegisterRangeReportPort;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateCashRegisterRangeReportUseCaseTest {

    @Test
    void should_generate_filtered_pdf_report() {
        CashRegisterRepository repository = mock(CashRegisterRepository.class);
        CashRegisterRangeReportPort reportPort = mock(CashRegisterRangeReportPort.class);
        var useCase = new GenerateCashRegisterRangeReportUseCase(repository, reportPort);
        var filter = new CashRegisterReportFilter(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        List<CashRegisterExportRow> rows = List.of();
        byte[] expected = {1, 2, 3};
        when(repository.findExportRows(filter)).thenReturn(rows);
        when(reportPort.generate(rows, filter, ReportFormat.PDF, ReportType.GROUPED)).thenReturn(expected);

        byte[] result = useCase.execute(filter, ReportFormat.PDF, ReportType.GROUPED);

        assertArrayEquals(expected, result);
        verify(repository).findExportRows(filter);
        verify(reportPort).generate(rows, filter, ReportFormat.PDF, ReportType.GROUPED);
    }
}
