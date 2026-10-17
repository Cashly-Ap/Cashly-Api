package com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.session.use_case;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.DailyTotals;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportFormat;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportType;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out.CashRegisterReportPort;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashMovement;
import com.cashly.cashly_api.finance.treasury.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateCashRegisterReportUseCaseTest {

    @Mock private CashRegisterQueryGateway queries;

    @Mock private CashRegisterRepository repository;
    @Mock private CashRegisterReportPort reportPort;

    private GenerateCashRegisterReportUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateCashRegisterReportUseCase(repository, queries, reportPort);
    }

    @Test
    void should_generate_detailed_excel_from_selected_session_ledger() {
        UUID id = UUID.randomUUID();
        CashRegister register = CashRegister.open(
                RegisterId.of(id), LocalDate.now(), UUID.randomUUID(), "Operator",
                new BigDecimal("100.00"));
        DailyTotals totals = new DailyTotals(
                new BigDecimal("70.00"), 1,
                BigDecimal.ZERO, 0,
                new BigDecimal("20.00"), 1,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, 0,
                BigDecimal.ZERO, new BigDecimal("70.00"), new BigDecimal("20.00"),
                List.of());
        when(repository.findById(RegisterId.of(id))).thenReturn(Optional.of(register));
        when(queries.findSessionTotals(register.getId())).thenReturn(totals);
        when(repository.findMovements(register.getId())).thenReturn(List.of(org.mockito.Mockito.mock(CashMovement.class)));
        when(reportPort.generateExcel(argThat(response ->
                response.expectedBalance().compareTo(new BigDecimal("150.00")) == 0),
                org.mockito.ArgumentMatchers.eq(ReportType.DETAILED)))
                .thenReturn(new byte[]{1, 2, 3});

        byte[] result = useCase.execute(id, ReportFormat.EXCEL, ReportType.DETAILED);

        assertArrayEquals(new byte[]{1, 2, 3}, result);
        verify(reportPort).generateExcel(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(ReportType.DETAILED));
    }

    @Test
    void should_preserve_stored_totals_when_legacy_session_has_no_ledger() {
        UUID id = UUID.randomUUID();
        CashRegister register = CashRegister.open(
                RegisterId.of(id), LocalDate.now(), UUID.randomUUID(), "Operator",
                new BigDecimal("100.00"));
        when(repository.findById(RegisterId.of(id))).thenReturn(Optional.of(register));
        when(repository.findMovements(register.getId())).thenReturn(List.of());
        when(reportPort.generatePdf(argThat(response ->
                response.expectedBalance().compareTo(new BigDecimal("100.00")) == 0),
                org.mockito.ArgumentMatchers.eq(ReportType.DETAILED)))
                .thenReturn(new byte[]{4, 5, 6});

        byte[] result = useCase.execute(id, ReportFormat.PDF, ReportType.DETAILED);

        assertArrayEquals(new byte[]{4, 5, 6}, result);
        verify(queries, org.mockito.Mockito.never()).findSessionTotals(register.getId());
    }
}
