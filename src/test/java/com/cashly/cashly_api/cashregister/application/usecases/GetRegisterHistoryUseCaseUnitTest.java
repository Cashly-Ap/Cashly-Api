package com.cashly.cashly_api.cashregister.application.usecases;

import com.cashly.cashly_api.cashregister.application.dto.CashRegisterMapper;
import com.cashly.cashly_api.cashregister.application.dto.CashRegisterResponse;
import com.cashly.cashly_api.cashregister.domain.entities.CashRegister;
import com.cashly.cashly_api.cashregister.domain.entities.CashMovement;
import com.cashly.cashly_api.cashregister.application.dto.DailyTotals;
import com.cashly.cashly_api.cashregister.domain.repositories.CashRegisterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetRegisterHistoryUseCase Unit Tests")
class GetRegisterHistoryUseCaseUnitTest {

    @Mock
    private CashRegisterRepository repository;

    private GetRegisterHistoryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetRegisterHistoryUseCase(repository);
    }

    @Test
    void should_return_paginated_history_when_no_filters() {
        var cashRegister = mock(CashRegister.class);
        var page = new PageImpl<>(List.of(cashRegister));

        when(repository.findAll(eq(0), eq(20), eq("closureDate,desc"), isNull(), isNull()))
                .thenReturn(page);

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(CashRegisterMapper.class)) {
            var expectedResponse = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister))
                    .thenReturn(expectedResponse);

            var result = useCase.execute(0, 20, "closureDate,desc", null, null);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(expectedResponse, result.getContent().getFirst());
            verify(repository).findAll(0, 20, "closureDate,desc", null, null);
        }
    }

    @Test
    void should_filter_by_date_range() {
        var cashRegister = mock(CashRegister.class);
        var page = new PageImpl<>(List.of(cashRegister));
        var dateFrom = LocalDate.of(2025, 1, 1);
        var dateTo = LocalDate.of(2025, 12, 31);

        when(repository.findAll(eq(0), eq(10), eq("closureDate,desc"), eq(dateFrom), eq(dateTo)))
                .thenReturn(page);

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(CashRegisterMapper.class)) {
            var expectedResponse = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister))
                    .thenReturn(expectedResponse);

            var result = useCase.execute(0, 10, "closureDate,desc", dateFrom, dateTo);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(repository).findAll(0, 10, "closureDate,desc", dateFrom, dateTo);
        }
    }

    @Test
    void should_filter_by_date_from_only() {
        var cashRegister = mock(CashRegister.class);
        var page = new PageImpl<>(List.of(cashRegister));
        var dateFrom = LocalDate.of(2025, 6, 1);

        when(repository.findAll(eq(0), eq(20), anyString(), eq(dateFrom), isNull()))
                .thenReturn(page);

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(CashRegisterMapper.class)) {
            var expectedResponse = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister))
                    .thenReturn(expectedResponse);

            var result = useCase.execute(0, 20, "closureDate,desc", dateFrom, null);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(repository).findAll(0, 20, "closureDate,desc", dateFrom, null);
        }
    }

    @Test
    void should_return_empty_page_when_no_results() {
        Page<CashRegister> emptyPage = new PageImpl<>(List.<CashRegister>of());

        when(repository.findAll(eq(0), eq(20), anyString(), isNull(), isNull()))
                .thenReturn(emptyPage);

        var result = useCase.execute(0, 20, "closureDate,desc", null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void should_include_session_movements_only_in_detailed_view() {
        var cashRegister = mock(CashRegister.class);
        var movements = List.of(mock(CashMovement.class));
        var page = new PageImpl<>(List.of(cashRegister));
        var totals = mock(DailyTotals.class);
        when(repository.findAll(0, 20, "openedAt,desc", null, null)).thenReturn(page);
        when(repository.findSessionTotals(cashRegister.getId())).thenReturn(totals);
        when(totals.totalRevenue()).thenReturn(java.math.BigDecimal.ZERO);
        when(totals.totalExpenses()).thenReturn(java.math.BigDecimal.ZERO);
        when(repository.findMovements(cashRegister.getId())).thenReturn(movements);

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(CashRegisterMapper.class)) {
            var expected = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister, totals, movements)).thenReturn(expected);

            var result = useCase.execute(0, 20, "openedAt,desc", null, null, true);

            assertEquals(expected, result.getContent().getFirst());
            verify(repository).findMovements(cashRegister.getId());
        }
    }

    @Test
    void should_use_session_totals_without_exposing_movements_in_simple_view() {
        var cashRegister = mock(CashRegister.class);
        var movements = List.of(mock(CashMovement.class));
        var page = new PageImpl<>(List.of(cashRegister));
        var totals = mock(DailyTotals.class);
        when(repository.findAll(0, 20, "openedAt,desc", null, null)).thenReturn(page);
        when(repository.findSessionTotals(cashRegister.getId())).thenReturn(totals);
        when(totals.totalRevenue()).thenReturn(java.math.BigDecimal.ZERO);
        when(totals.totalExpenses()).thenReturn(java.math.BigDecimal.ZERO);
        when(repository.findMovements(cashRegister.getId())).thenReturn(movements);

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(CashRegisterMapper.class)) {
            var expected = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister, totals, List.of()))
                    .thenReturn(expected);

            var result = useCase.execute(0, 20, "openedAt,desc", null, null, false);

            assertEquals(expected, result.getContent().getFirst());
        }
    }

    @Test
    void should_preserve_stored_totals_for_legacy_session_without_movements() {
        var cashRegister = mock(CashRegister.class);
        var page = new PageImpl<>(List.of(cashRegister));
        when(repository.findAll(0, 20, "openedAt,desc", null, null)).thenReturn(page);
        when(repository.findMovements(cashRegister.getId())).thenReturn(List.of());

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(CashRegisterMapper.class)) {
            var expected = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister)).thenReturn(expected);

            var result = useCase.execute(0, 20, "openedAt,desc", null, null, true);

            assertEquals(expected, result.getContent().getFirst());
            verify(repository, never()).findSessionTotals(any());
        }
    }
}
