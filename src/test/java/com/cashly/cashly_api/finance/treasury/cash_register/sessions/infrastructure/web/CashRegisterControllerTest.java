package com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.close.request.CloseRegisterRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.open.request.OpenRegisterRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.close.use_case.CloseRegisterUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.queries.use_case.GetActiveRegisterUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.queries.use_case.GetRegisterHistoryUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.open.use_case.OpenRegisterUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case.RecordCashMovementUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.use_case.CancelCashMovementUseCase;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.request.CancelCashMovementRequest;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.response.CashMovementResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashRegisterController Unit Tests")
class CashRegisterControllerTest {

    @Mock
    private OpenRegisterUseCase openRegisterUseCase;

    @Mock
    private GetActiveRegisterUseCase getActiveRegisterUseCase;

    @Mock
    private CloseRegisterUseCase closeRegisterUseCase;

    @Mock
    private GetRegisterHistoryUseCase getRegisterHistoryUseCase;

    @Mock
    private RecordCashMovementUseCase recordCashMovementUseCase;

    @Mock
    private CancelCashMovementUseCase cancelCashMovementUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CashRegisterController cashRegisterController;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @Test
    @DisplayName("should open register and return 201")
    void should_open_register_and_return_201() {
        when(authentication.getPrincipal())
                .thenReturn(authenticatedUserId.toString());

        CashRegisterResponse response = mock(CashRegisterResponse.class);

        when(openRegisterUseCase.execute(any(UUID.class), anyString(), any()))
                .thenReturn(response);

        OpenRegisterRequest request = new OpenRegisterRequest("Test User",
                java.math.BigDecimal.ZERO);

        ResponseEntity<CashRegisterResponse> result = cashRegisterController
                .open(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(openRegisterUseCase).execute(any(UUID.class), eq("Test User"),
                eq(java.math.BigDecimal.ZERO));
    }

    @Test
    @DisplayName("should get active register and return 200")
    void should_get_active_register_and_return_200() {
        CashRegisterResponse response = mock(CashRegisterResponse.class);
        when(getActiveRegisterUseCase.execute())
                .thenReturn(java.util.Optional.of(response));

        ResponseEntity<CashRegisterResponse> result = cashRegisterController
                .getActive();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(getActiveRegisterUseCase).execute();
    }

    @Test
    @DisplayName("should close register and return 200")
    void should_close_register_and_return_200() {
        when(authentication.getPrincipal())
                .thenReturn(authenticatedUserId.toString());

        UUID closureId = UUID.randomUUID();
        CashRegisterResponse response = mock(CashRegisterResponse.class);
        when(closeRegisterUseCase.execute(any(UUID.class), any(UUID.class),
                anyString(), any(), anyString())).thenReturn(response);

        CloseRegisterRequest request = new CloseRegisterRequest("Test User",
                java.math.BigDecimal.ZERO, "Closing notes");

        ResponseEntity<CashRegisterResponse> result = cashRegisterController
                .close(closureId, request, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(closeRegisterUseCase).execute(eq(closureId),
                eq(authenticatedUserId), eq("Test User"),
                eq(java.math.BigDecimal.ZERO), eq("Closing notes"));
    }

    @Test
    @DisplayName("should cancel movement and return 200")
    void should_cancel_movement_and_return_200() {
        when(authentication.getPrincipal())
                .thenReturn(authenticatedUserId.toString());
        UUID movementId = UUID.randomUUID();
        CashMovementResponse response = mock(CashMovementResponse.class);
        when(cancelCashMovementUseCase.execute(eq(movementId),
                any(CancelCashMovementRequest.class),
                eq(authenticatedUserId))).thenReturn(response);

        ResponseEntity<CashMovementResponse> result = cashRegisterController
                .cancelMovement(movementId,
                        new CancelCashMovementRequest("Duplicado"),
                        authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(cancelCashMovementUseCase).execute(eq(movementId),
                any(CancelCashMovementRequest.class),
                eq(authenticatedUserId));
    }

    @Test
    @DisplayName("should get history and return 200")
    void should_get_history_and_return_200() {
        CashRegisterResponse response = mock(CashRegisterResponse.class);
        Page<CashRegisterResponse> page = new PageImpl<>(List.of(response));

        when(getRegisterHistoryUseCase.execute(anyInt(), anyInt(), anyString(),
                any(), any(), any(), anyBoolean()))
                .thenReturn(PageResponseMapper.from(page));

        ResponseEntity<Page<CashRegisterResponse>> result = cashRegisterController
                .getHistory(0, 20, "closureDate,desc", null, null, null, true);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
        verify(getRegisterHistoryUseCase).execute(0, 20, "closureDate,desc",
                null, null, null, true);
    }

    @Test
    @DisplayName("should get empty history when no registers")
    void should_get_empty_history_when_no_registers() {
        Page<CashRegisterResponse> emptyPage = Page.empty();

        when(getRegisterHistoryUseCase.execute(anyInt(), anyInt(), anyString(),
                any(), any(), any(), anyBoolean()))
                .thenReturn(PageResponseMapper.from(emptyPage));

        ResponseEntity<Page<CashRegisterResponse>> result = cashRegisterController
                .getHistory(0, 20, "closureDate,desc", null, null, null, false);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(getRegisterHistoryUseCase).execute(0, 20, "closureDate,desc",
                null, null, null, false);
    }
    @Test
    void should_preserve_web_history_page_and_sort_metadata() {
        var response = mock(CashRegisterResponse.class);
        when(getRegisterHistoryUseCase.execute(1, 5, "openedAt,desc", null,
                null, null, false))
                .thenReturn(new com.cashly.cashly_api.shared.dto.PageResponse<>(
                        List.of(response), 12L, 3, 5, 1, false, false, false));
        var page = cashRegisterController
                .getHistory(1, 5, "openedAt,desc", null, null, null, false)
                .getBody();
        assertNotNull(page);
        assertEquals(1, page.getNumber());
        assertEquals(5, page.getSize());
        assertEquals(12L, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
        assertFalse(page.isFirst());
        assertFalse(page.isLast());
        assertEquals(org.springframework.data.domain.Sort.Direction.DESC,
                page.getSort().getOrderFor("openedAt").getDirection());
    }
}
