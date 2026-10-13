package com.cashly.cashly_api.orders.infrastructure.adapters.in.controllers;

import com.cashly.cashly_api.orders.application.dto.request.CancelOrderRequest;
import com.cashly.cashly_api.orders.application.dto.request.ConvertToInvoiceRequest;
import com.cashly.cashly_api.orders.application.dto.request.CreateOrderRequest;
import com.cashly.cashly_api.orders.application.dto.request.UpdateOrderRequest;
import com.cashly.cashly_api.orders.application.dto.response.ConvertToInvoiceResponse;
import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.orders.application.dto.response.OrderListResponse;
import com.cashly.cashly_api.orders.application.usecases.CancelOrderUseCase;
import com.cashly.cashly_api.orders.application.usecases.ConfirmOrderUseCase;
import com.cashly.cashly_api.orders.application.usecases.ConvertToInvoiceUseCase;
import com.cashly.cashly_api.orders.application.usecases.CreateOrderUseCase;
import com.cashly.cashly_api.orders.application.usecases.DeleteOrderUseCase;
import com.cashly.cashly_api.orders.application.usecases.GetOrderByIdUseCase;
import com.cashly.cashly_api.orders.application.usecases.GetOrdersUseCase;
import com.cashly.cashly_api.orders.application.usecases.UpdateOrderUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Unit Tests")
class OrderControllerTest {

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    @Mock
    private GetOrderByIdUseCase getOrderByIdUseCase;

    @Mock
    private GetOrdersUseCase getOrdersUseCase;

    @Mock
    private UpdateOrderUseCase updateOrderUseCase;

    @Mock
    private ConfirmOrderUseCase confirmOrderUseCase;

    @Mock
    private CancelOrderUseCase cancelOrderUseCase;

    @Mock
    private ConvertToInvoiceUseCase convertToInvoiceUseCase;

    @Mock
    private DeleteOrderUseCase deleteOrderUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderController orderController;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @Test
    @DisplayName("should create order and return 201")
    void should_create_order_and_return_201() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());

        OrderDetailResponse response = mock(OrderDetailResponse.class);

        when(createOrderUseCase.execute(any(CreateOrderRequest.class), any(UUID.class)))
                .thenReturn(response);

        var request = new CreateOrderRequest(UUID.randomUUID(), null, List.of());

        ResponseEntity<OrderDetailResponse> result = orderController.create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(createOrderUseCase).execute(any(CreateOrderRequest.class), eq(authenticatedUserId));
    }

    @Test
    @DisplayName("should get all orders and return 200")
    void should_get_all_orders_and_return_200() {
        OrderListResponse order = new OrderListResponse(
                UUID.randomUUID(), "ORD-00001", UUID.randomUUID(), "Customer",
                "PENDING", BigDecimal.TEN, 2, null, LocalDateTime.now());

        when(getOrdersUseCase.execute(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(order));

        ResponseEntity<List<OrderListResponse>> result = orderController.getAll(
                null, null, null, null, null, null, "createdAt", "desc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        verify(getOrdersUseCase).execute(null, null, null, null, null, null, "createdAt", "desc");
    }

    @Test
    @DisplayName("should return empty list when no orders")
    void should_return_empty_list_when_no_orders() {
        when(getOrdersUseCase.execute(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        ResponseEntity<List<OrderListResponse>> result = orderController.getAll(
                null, null, null, null, null, null, "createdAt", "desc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(getOrdersUseCase).execute(null, null, null, null, null, null, "createdAt", "desc");
    }

    @Test
    @DisplayName("should get order by id and return 200")
    void should_get_order_by_id_and_return_200() {
        UUID id = UUID.randomUUID();
        OrderDetailResponse response = mock(OrderDetailResponse.class);
        when(response.id()).thenReturn(id);

        when(getOrderByIdUseCase.execute(id)).thenReturn(response);

        ResponseEntity<OrderDetailResponse> result = orderController.getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getOrderByIdUseCase).execute(id);
    }

    @Test
    @DisplayName("should update order and return 200")
    void should_update_order_and_return_200() {
        UUID id = UUID.randomUUID();
        OrderDetailResponse response = mock(OrderDetailResponse.class);

        when(updateOrderUseCase.execute(eq(id), any(UpdateOrderRequest.class))).thenReturn(response);

        var request = new UpdateOrderRequest(null, List.of());

        ResponseEntity<OrderDetailResponse> result = orderController.update(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(updateOrderUseCase).execute(eq(id), any(UpdateOrderRequest.class));
    }

    @Test
    @DisplayName("should confirm order and return 200")
    void should_confirm_order_and_return_200() {
        UUID id = UUID.randomUUID();
        OrderDetailResponse response = mock(OrderDetailResponse.class);

        when(confirmOrderUseCase.execute(id)).thenReturn(response);

        ResponseEntity<OrderDetailResponse> result = orderController.confirm(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(confirmOrderUseCase).execute(id);
    }

    @Test
    @DisplayName("should cancel order and return 200")
    void should_cancel_order_and_return_200() {
        UUID id = UUID.randomUUID();
        OrderDetailResponse response = mock(OrderDetailResponse.class);

        when(cancelOrderUseCase.execute(eq(id), any(CancelOrderRequest.class))).thenReturn(response);

        CancelOrderRequest request = new CancelOrderRequest("Test reason");

        ResponseEntity<OrderDetailResponse> result = orderController.cancel(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(cancelOrderUseCase).execute(eq(id), any(CancelOrderRequest.class));
    }

    @Test
    @DisplayName("should convert order to invoice and return 200")
    void should_convert_order_to_invoice_and_return_200() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());

        UUID id = UUID.randomUUID();
        ConvertToInvoiceResponse response = new ConvertToInvoiceResponse(
                id, "ORD-00001", "CONVERTED", UUID.randomUUID(), "FAC-00001");

        when(convertToInvoiceUseCase.execute(eq(id), any(ConvertToInvoiceRequest.class), eq(authenticatedUserId)))
                .thenReturn(response);

        ConvertToInvoiceRequest request = new ConvertToInvoiceRequest("CREDIT", null, null, null, null);

        ResponseEntity<ConvertToInvoiceResponse> result = orderController.convertToInvoice(id, request, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(id, result.getBody().orderId());
        verify(convertToInvoiceUseCase).execute(eq(id), any(ConvertToInvoiceRequest.class), eq(authenticatedUserId));
    }

    @Test
    @DisplayName("should delete order and return 204")
    void should_delete_order_and_return_204() {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteOrderUseCase).execute(id);

        ResponseEntity<Void> result = orderController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deleteOrderUseCase).execute(id);
    }


}
