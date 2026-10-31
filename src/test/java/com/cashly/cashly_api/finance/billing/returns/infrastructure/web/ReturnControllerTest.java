package com.cashly.cashly_api.finance.billing.returns.infrastructure.web;

import com.cashly.cashly_api.finance.billing.returns.application.create.request.CreateReturnRequest;
import com.cashly.cashly_api.finance.billing.returns.application.dto.response.ReturnListResponse;
import com.cashly.cashly_api.finance.billing.returns.application.dto.response.ReturnResponse;
import com.cashly.cashly_api.finance.billing.returns.application.create.use_case.CreateReturnUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.delete.use_case.DeleteReturnUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.list.use_case.GetAllReturnsUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.get.use_case.GetReturnByIdUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnController Unit Tests")
class ReturnControllerTest {

    @Mock
    private CreateReturnUseCase createReturnUseCase;

    @Mock
    private GetAllReturnsUseCase getAllReturnsUseCase;

    @Mock
    private GetReturnByIdUseCase getReturnByIdUseCase;

    @Mock
    private DeleteReturnUseCase deleteReturnUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReturnController returnController;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("should create return and return 201")
    void shouldCreateReturn() {
        when(authentication.getPrincipal())
                .thenReturn(authenticatedUserId.toString());
        ReturnResponse response = new ReturnResponse(UUID.randomUUID(),
                "RET-2026-0001", UUID.randomUUID(), "INV-2026-00001",
                UUID.randomUUID(), "Cliente Test", "J-12345678-9", "AUTOMATIC",
                "Producto defectuoso", BigDecimal.TEN, UUID.randomUUID(),
                "Test User", LocalDateTime.now(), LocalDateTime.now(),
                List.of());

        when(createReturnUseCase.execute(any(CreateReturnRequest.class),
                any(UUID.class), anyString())).thenReturn(response);

        CreateReturnRequest request = new CreateReturnRequest(UUID.randomUUID(),
                UUID.randomUUID(), "AUTOMATIC", "Producto defectuoso",
                List.of(new CreateReturnRequest.ReturnLineRequest(
                        UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE,
                        BigDecimal.TEN, BigDecimal.TEN, null,
                        List.of(new CreateReturnRequest.ReturnLineRequest.AllocationRequest(
                                UUID.randomUUID(), BigDecimal.ONE)))));

        ResponseEntity<ReturnResponse> result = returnController.create(request,
                authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("RET-2026-0001", result.getBody().returnNumber());
        verify(createReturnUseCase).execute(any(CreateReturnRequest.class),
                any(UUID.class), anyString());
    }

    @Test
    @DisplayName("should list returns and return 200")
    void shouldListReturns() {
        ReturnListResponse returnItem = new ReturnListResponse(
                UUID.randomUUID(), "RET-2026-0001", UUID.randomUUID(),
                "INV-2026-00001", UUID.randomUUID(), "Cliente Test",
                "AUTOMATIC", "Producto defectuoso", BigDecimal.TEN, 1,
                LocalDateTime.now());

        when(getAllReturnsUseCase.execute(null, null, null, null))
                .thenReturn(List.of(returnItem));

        ResponseEntity<List<ReturnListResponse>> result = returnController
                .getAll(null, null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("RET-2026-0001", result.getBody().get(0).returnNumber());
        verify(getAllReturnsUseCase).execute(null, null, null, null);
    }

    @Test
    @DisplayName("should list returns with filters and return 200")
    void shouldListReturnsWithFilters() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);
        UUID customerId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();

        when(getAllReturnsUseCase.execute(dateFrom, dateTo, customerId,
                invoiceId)).thenReturn(List.of());

        ResponseEntity<List<ReturnListResponse>> result = returnController
                .getAll(dateFrom, dateTo, customerId, invoiceId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(getAllReturnsUseCase).execute(dateFrom, dateTo, customerId,
                invoiceId);
    }

    @Test
    @DisplayName("should get return by id and return 200")
    void shouldGetReturnById() {
        UUID id = UUID.randomUUID();
        ReturnResponse response = new ReturnResponse(id, "RET-2026-0001",
                UUID.randomUUID(), "INV-2026-00001", UUID.randomUUID(),
                "Cliente Test", "J-12345678-9", "AUTOMATIC",
                "Producto defectuoso", BigDecimal.TEN, UUID.randomUUID(),
                "Test User", LocalDateTime.now(), LocalDateTime.now(),
                List.of());

        when(getReturnByIdUseCase.execute(id)).thenReturn(response);

        ResponseEntity<ReturnResponse> result = returnController.getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getReturnByIdUseCase).execute(id);
    }

    @Test
    @DisplayName("should throw exception when return not found")
    void shouldThrowWhenReturnNotFound() {
        UUID id = UUID.randomUUID();

        when(getReturnByIdUseCase.execute(id)).thenThrow(
                new GetReturnByIdUseCase.ReturnNotFoundException(id));

        assertThrows(GetReturnByIdUseCase.ReturnNotFoundException.class,
                () -> returnController.getById(id));
        verify(getReturnByIdUseCase).execute(id);
    }

    @Test
    @DisplayName("should delete return and return 204")
    void shouldDeleteReturn() {
        UUID id = UUID.randomUUID();
        when(authentication.getPrincipal())
                .thenReturn(authenticatedUserId.toString());
        doNothing().when(deleteReturnUseCase).execute(any(UUID.class),
                any(UUID.class), anyString());

        ResponseEntity<Void> result = returnController.delete(id,
                authentication);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deleteReturnUseCase).execute(any(UUID.class), any(UUID.class),
                anyString());
    }

    @Test
    @DisplayName("should throw exception when deleting non-existent return")
    void shouldThrowWhenDeletingNonExistentReturn() {
        UUID id = UUID.randomUUID();
        when(authentication.getPrincipal())
                .thenReturn(authenticatedUserId.toString());

        doThrow(new GetReturnByIdUseCase.ReturnNotFoundException(id))
                .when(deleteReturnUseCase)
                .execute(any(UUID.class), any(UUID.class), anyString());

        assertThrows(GetReturnByIdUseCase.ReturnNotFoundException.class,
                () -> returnController.delete(id, authentication));
        verify(deleteReturnUseCase).execute(any(UUID.class), any(UUID.class),
                anyString());
    }

    @Test
    @DisplayName("should return empty list when no returns exist")
    void shouldReturnEmptyListWhenNoReturns() {
        when(getAllReturnsUseCase.execute(null, null, null, null))
                .thenReturn(List.of());

        ResponseEntity<List<ReturnListResponse>> result = returnController
                .getAll(null, null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }
}
