package com.parsun.parsun_api.cashregister.infrastructure.web;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterResponse;
import com.parsun.parsun_api.cashregister.application.dto.CloseRegisterRequest;
import com.parsun.parsun_api.cashregister.application.dto.OpenRegisterRequest;
import com.parsun.parsun_api.cashregister.application.usecases.CloseRegisterUseCase;
import com.parsun.parsun_api.cashregister.application.usecases.GetActiveRegisterUseCase;
import com.parsun.parsun_api.cashregister.application.usecases.GetRegisterHistoryUseCase;
import com.parsun.parsun_api.cashregister.application.usecases.OpenRegisterUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
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
    private Authentication authentication;

    @InjectMocks
    private CashRegisterController cashRegisterController;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @Test
    @DisplayName("should open register and return 201")
    void should_open_register_and_return_201() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());

        CashRegisterResponse response = mock(CashRegisterResponse.class);

        when(openRegisterUseCase.execute(any(UUID.class), anyString())).thenReturn(response);

        OpenRegisterRequest request = new OpenRegisterRequest("Test User");

        ResponseEntity<CashRegisterResponse> result = cashRegisterController.open(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(openRegisterUseCase).execute(any(UUID.class), eq("Test User"));
    }

    @Test
    @DisplayName("should get active register and return 200")
    void should_get_active_register_and_return_200() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());

        CashRegisterResponse response = mock(CashRegisterResponse.class);
        when(getActiveRegisterUseCase.execute(any(UUID.class))).thenReturn(response);

        ResponseEntity<CashRegisterResponse> result = cashRegisterController.getActive(authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(getActiveRegisterUseCase).execute(authenticatedUserId);
    }

    @Test
    @DisplayName("should close register and return 200")
    void should_close_register_and_return_200() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());

        UUID closureId = UUID.randomUUID();
        CashRegisterResponse response = mock(CashRegisterResponse.class);
        when(closeRegisterUseCase.execute(any(UUID.class), any(UUID.class), anyString(), anyString()))
                .thenReturn(response);

        CloseRegisterRequest request = new CloseRegisterRequest("Test User", "Closing notes");

        ResponseEntity<CashRegisterResponse> result = cashRegisterController.close(closureId, request, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(closeRegisterUseCase).execute(eq(closureId), eq(authenticatedUserId), eq("Test User"), eq("Closing notes"));
    }

    @Test
    @DisplayName("should get history and return 200")
    void should_get_history_and_return_200() {
        CashRegisterResponse response = mock(CashRegisterResponse.class);
        Page<CashRegisterResponse> page = new PageImpl<>(List.of(response));

        when(getRegisterHistoryUseCase.execute(anyInt(), anyInt(), anyString(), any(), any()))
                .thenReturn(page);

        ResponseEntity<Page<CashRegisterResponse>> result = cashRegisterController.getHistory(
                0, 20, "closureDate,desc", null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getContent().size());
        verify(getRegisterHistoryUseCase).execute(0, 20, "closureDate,desc", null, null);
    }

    @Test
    @DisplayName("should get empty history when no registers")
    void should_get_empty_history_when_no_registers() {
        Page<CashRegisterResponse> emptyPage = Page.empty();

        when(getRegisterHistoryUseCase.execute(anyInt(), anyInt(), anyString(), any(), any()))
                .thenReturn(emptyPage);

        ResponseEntity<Page<CashRegisterResponse>> result = cashRegisterController.getHistory(
                0, 20, "closureDate,desc", null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(getRegisterHistoryUseCase).execute(0, 20, "closureDate,desc", null, null);
    }
}
