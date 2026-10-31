package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.queries.use_case;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.mappers.CashRegisterMapper;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashMovement;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.DailyTotals;
import com.cashly.cashly_api.finance.treasury.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;
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
    private CashRegisterQueryGateway queries;

    @Mock
    private CashRegisterRepository repository;

    private GetRegisterHistoryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetRegisterHistoryUseCase(repository, queries);
    }

    @Test
    void should_return_paginated_history_when_no_filters() {
        var cashRegister = mock(CashRegister.class);
        var page = new PageImpl<>(List.of(cashRegister));

        when(queries.findAll(eq(0), eq(20), eq("closureDate,desc"), isNull(),
                isNull())).thenReturn(PageResponseMapper.from(page));

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(
                CashRegisterMapper.class)) {
            var expectedResponse = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister))
                    .thenReturn(expectedResponse);

            var result = useCase.execute(0, 20, "closureDate,desc", null, null);

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(expectedResponse, result.content().getFirst());
            verify(queries).findAll(0, 20, "closureDate,desc", null, null);
        }
    }

    @Test
    void should_filter_by_date_range() {
        var cashRegister = mock(CashRegister.class);
        var page = new PageImpl<>(List.of(cashRegister));
        var dateFrom = LocalDate.of(2025, 1, 1);
        var dateTo = LocalDate.of(2025, 12, 31);

        when(queries.findAll(eq(0), eq(10), eq("closureDate,desc"),
                eq(dateFrom), eq(dateTo)))
                .thenReturn(PageResponseMapper.from(page));

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(
                CashRegisterMapper.class)) {
            var expectedResponse = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister))
                    .thenReturn(expectedResponse);

            var result = useCase.execute(0, 10, "closureDate,desc", dateFrom,
                    dateTo);

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            verify(queries).findAll(0, 10, "closureDate,desc", dateFrom,
                    dateTo);
        }
    }

    @Test
    void should_filter_by_date_from_only() {
        var cashRegister = mock(CashRegister.class);
        var page = new PageImpl<>(List.of(cashRegister));
        var dateFrom = LocalDate.of(2025, 6, 1);

        when(queries.findAll(eq(0), eq(20), anyString(), eq(dateFrom),
                isNull())).thenReturn(PageResponseMapper.from(page));

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(
                CashRegisterMapper.class)) {
            var expectedResponse = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister))
                    .thenReturn(expectedResponse);

            var result = useCase.execute(0, 20, "closureDate,desc", dateFrom,
                    null);

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            verify(queries).findAll(0, 20, "closureDate,desc", dateFrom, null);
        }
    }

    @Test
    void should_return_empty_page_when_no_results() {
        org.springframework.data.domain.Page<CashRegister> emptyPage = new PageImpl<>(
                List.<CashRegister>of());

        when(queries.findAll(eq(0), eq(20), anyString(), isNull(), isNull()))
                .thenReturn(PageResponseMapper.from(emptyPage));

        var result = useCase.execute(0, 20, "closureDate,desc", null, null);

        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertEquals(0, result.content().size());
    }

    @Test
    void should_include_session_movements_only_in_detailed_view() {
        var cashRegister = mock(CashRegister.class);
        var movements = List.of(mock(CashMovement.class));
        var page = new PageImpl<>(List.of(cashRegister));
        var totals = mock(DailyTotals.class);
        when(queries.findAll(0, 20, "openedAt,desc", null, null))
                .thenReturn(PageResponseMapper.from(page));
        when(queries.findSessionTotals(cashRegister.getId()))
                .thenReturn(totals);
        when(totals.totalRevenue()).thenReturn(java.math.BigDecimal.ZERO);
        when(totals.totalExpenses()).thenReturn(java.math.BigDecimal.ZERO);
        when(repository.findMovements(cashRegister.getId()))
                .thenReturn(movements);

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(
                CashRegisterMapper.class)) {
            var expected = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister,
                    totals, movements)).thenReturn(expected);

            var result = useCase.execute(0, 20, "openedAt,desc", null, null,
                    true);

            assertEquals(expected, result.content().getFirst());
            verify(repository).findMovements(cashRegister.getId());
        }
    }

    @Test
    void should_use_session_totals_without_exposing_movements_in_simple_view() {
        var cashRegister = mock(CashRegister.class);
        var movements = List.of(mock(CashMovement.class));
        var page = new PageImpl<>(List.of(cashRegister));
        var totals = mock(DailyTotals.class);
        when(queries.findAll(0, 20, "openedAt,desc", null, null))
                .thenReturn(PageResponseMapper.from(page));
        when(queries.findSessionTotals(cashRegister.getId()))
                .thenReturn(totals);
        when(totals.totalRevenue()).thenReturn(java.math.BigDecimal.ZERO);
        when(totals.totalExpenses()).thenReturn(java.math.BigDecimal.ZERO);
        when(repository.findMovements(cashRegister.getId()))
                .thenReturn(movements);

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(
                CashRegisterMapper.class)) {
            var expected = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister,
                    totals, List.of())).thenReturn(expected);

            var result = useCase.execute(0, 20, "openedAt,desc", null, null,
                    false);

            assertEquals(expected, result.content().getFirst());
        }
    }

    @Test
    void should_preserve_stored_totals_for_legacy_session_without_movements() {
        var cashRegister = mock(CashRegister.class);
        var page = new PageImpl<>(List.of(cashRegister));
        when(queries.findAll(0, 20, "openedAt,desc", null, null))
                .thenReturn(PageResponseMapper.from(page));
        when(repository.findMovements(cashRegister.getId()))
                .thenReturn(List.of());

        try (MockedStatic<CashRegisterMapper> mockedMapper = mockStatic(
                CashRegisterMapper.class)) {
            var expected = mock(CashRegisterResponse.class);
            mockedMapper.when(() -> CashRegisterMapper.toResponse(cashRegister))
                    .thenReturn(expected);

            var result = useCase.execute(0, 20, "openedAt,desc", null, null,
                    true);

            assertEquals(expected, result.content().getFirst());
            verify(queries, never()).findSessionTotals(any());
        }
    }
    @Test
    void should_preserve_all_history_pagination_metadata() {
        var cashRegister = mock(CashRegister.class);
        var response = mock(CashRegisterResponse.class);
        var page = new PageResponse<>(List.of(cashRegister), 12L, 3, 5, 1,
                false, false, false);
        when(queries.findAll(1, 5, "openedAt,desc", null, null))
                .thenReturn(page);
        when(repository.findMovements(cashRegister.getId()))
                .thenReturn(List.of());
        try (MockedStatic<CashRegisterMapper> mapper = mockStatic(
                CashRegisterMapper.class)) {
            mapper.when(() -> CashRegisterMapper.toResponse(cashRegister))
                    .thenReturn(response);
            assertEquals(
                    new PageResponse<>(List.of(response), 12L, 3, 5, 1, false,
                            false, false),
                    useCase.execute(1, 5, "openedAt,desc", null, null));
        }
    }
}
