package com.cashly.cashly_api.finance.treasury.payment_methods.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.payment_methods.application.create.request.CreatePaymentMethodRequest;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.update.request.UpdatePaymentMethodRequest;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.create.use_case.CreatePaymentMethodUseCase;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.delete.use_case.DeletePaymentMethodUseCase;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.queries.use_case.GetPaymentMethodByIdUseCase;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.queries.use_case.ListPaymentMethodsUseCase;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.toggle.use_case.TogglePaymentMethodUseCase;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.update.use_case.UpdatePaymentMethodUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentMethodController Unit Tests")
class PaymentMethodControllerTest {

    @Mock
    private CreatePaymentMethodUseCase createPaymentMethodUseCase;

    @Mock
    private UpdatePaymentMethodUseCase updatePaymentMethodUseCase;

    @Mock
    private ListPaymentMethodsUseCase listPaymentMethodsUseCase;

    @Mock
    private GetPaymentMethodByIdUseCase getPaymentMethodByIdUseCase;

    @Mock
    private TogglePaymentMethodUseCase togglePaymentMethodUseCase;

    @Mock
    private DeletePaymentMethodUseCase deletePaymentMethodUseCase;

    @InjectMocks
    private PaymentMethodController paymentMethodController;

    @Test
    @DisplayName("should create payment method and return 201")
    void should_create_payment_method_and_return_201() {
        UUID id = UUID.randomUUID();
        PaymentMethodResponse response = new PaymentMethodResponse(
                id, "CASH", "Cash", "Cash payment", true, false,
                LocalDateTime.now(), LocalDateTime.now());

        when(createPaymentMethodUseCase.execute(any(CreatePaymentMethodRequest.class))).thenReturn(response);

        CreatePaymentMethodRequest request = new CreatePaymentMethodRequest("Cash", "Cash payment");

        ResponseEntity<PaymentMethodResponse> result = paymentMethodController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(createPaymentMethodUseCase).execute(any(CreatePaymentMethodRequest.class));
    }

    @Test
    @DisplayName("should list payment methods and return 200")
    void should_list_payment_methods_and_return_200() {
        PaymentMethodResponse pm = new PaymentMethodResponse(
                UUID.randomUUID(), "CASH", "Cash", null, true, false,
                LocalDateTime.now(), LocalDateTime.now());

        PageResponse<PaymentMethodResponse> pageResult = new PageResponse<>(
                List.of(pm), 1, 1, 10, 0, true, true, false);
        when(listPaymentMethodsUseCase.execute(isNull(), isNull(), eq(0), eq(10), eq("name"), eq("asc")))
                .thenReturn(pageResult);

        ResponseEntity<PageResponse<PaymentMethodResponse>> result =
                paymentMethodController.list(null, null, 0, 10, "name", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().content().size());
    }

    @Test
    @DisplayName("should return empty list when no payment methods")
    void should_return_empty_list_when_no_payment_methods() {
        PageResponse<PaymentMethodResponse> pageResult = new PageResponse<>(
                List.of(), 0, 0, 10, 0, true, true, true);
        when(listPaymentMethodsUseCase.execute(any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(pageResult);

        ResponseEntity<PageResponse<PaymentMethodResponse>> result =
                paymentMethodController.list(null, null, 0, 10, "name", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().content().isEmpty());
    }

    @Test
    @DisplayName("should get payment method by id and return 200")
    void should_get_payment_method_by_id_and_return_200() {
        UUID id = UUID.randomUUID();
        PaymentMethodResponse response = new PaymentMethodResponse(
                id, "CASH", "Cash", null, true, false,
                LocalDateTime.now(), LocalDateTime.now());

        when(getPaymentMethodByIdUseCase.execute(id)).thenReturn(response);

        ResponseEntity<PaymentMethodResponse> result = paymentMethodController.getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getPaymentMethodByIdUseCase).execute(id);
    }

    @Test
    @DisplayName("should update payment method and return 200")
    void should_update_payment_method_and_return_200() {
        UUID id = UUID.randomUUID();
        PaymentMethodResponse response = new PaymentMethodResponse(
                id, "TRANSFER", "Transfer", null, true, false,
                LocalDateTime.now(), LocalDateTime.now());

        when(updatePaymentMethodUseCase.execute(eq(id), any(UpdatePaymentMethodRequest.class)))
                .thenReturn(response);

        UpdatePaymentMethodRequest request = new UpdatePaymentMethodRequest("Transfer", null, true);

        ResponseEntity<PaymentMethodResponse> result = paymentMethodController.update(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(updatePaymentMethodUseCase).execute(eq(id), any(UpdatePaymentMethodRequest.class));
    }

    @Test
    @DisplayName("should toggle payment method and return 200")
    void should_toggle_payment_method_and_return_200() {
        UUID id = UUID.randomUUID();
        PaymentMethodResponse response = new PaymentMethodResponse(
                id, "CASH", "Cash", null, false, false,
                LocalDateTime.now(), LocalDateTime.now());

        when(togglePaymentMethodUseCase.execute(id)).thenReturn(response);

        ResponseEntity<PaymentMethodResponse> result = paymentMethodController.toggle(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertFalse(result.getBody().isActive());
        verify(togglePaymentMethodUseCase).execute(id);
    }

    @Test
    @DisplayName("should delete payment method and return 204")
    void should_delete_payment_method_and_return_204() {
        UUID id = UUID.randomUUID();
        doNothing().when(deletePaymentMethodUseCase).execute(id);

        ResponseEntity<Void> result = paymentMethodController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deletePaymentMethodUseCase).execute(id);
    }
}
