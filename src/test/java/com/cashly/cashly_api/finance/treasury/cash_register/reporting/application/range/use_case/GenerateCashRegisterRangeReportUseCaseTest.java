package com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.range.use_case;

import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterExportRow;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterReportFilter;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportFormat;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportType;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out.CashRegisterRangeReportPort;
import com.cashly.cashly_api.finance.treasury.cash_register.application.ports.out.CashRegisterQueryGateway;
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
        CashRegisterQueryGateway repository = mock(
                CashRegisterQueryGateway.class);
        CashRegisterRangeReportPort reportPort = mock(
                CashRegisterRangeReportPort.class);
        var useCase = new GenerateCashRegisterRangeReportUseCase(repository,
                reportPort);
        var filter = new CashRegisterReportFilter(LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID());
        List<CashRegisterExportRow> rows = List.of();
        byte[] expected = {1, 2, 3};
        when(repository.findExportRows(filter)).thenReturn(rows);
        when(reportPort.generate(rows, filter, ReportFormat.PDF,
                ReportType.GROUPED)).thenReturn(expected);

        byte[] result = useCase.execute(filter, ReportFormat.PDF,
                ReportType.GROUPED);

        assertArrayEquals(expected, result);
        verify(repository).findExportRows(filter);
        verify(reportPort).generate(rows, filter, ReportFormat.PDF,
                ReportType.GROUPED);
    }
}
