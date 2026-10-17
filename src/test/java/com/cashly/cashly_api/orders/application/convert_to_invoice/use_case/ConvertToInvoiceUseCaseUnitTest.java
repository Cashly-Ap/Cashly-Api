package com.cashly.cashly_api.orders.application.convert_to_invoice.use_case;

import com.cashly.cashly_api.finance.billing.invoices.application.create.request.CreateInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.application.create.use_case.CreateInvoiceUseCase;
import com.cashly.cashly_api.orders.application.convert_to_invoice.request.ConvertToInvoiceRequest;
import com.cashly.cashly_api.orders.application.convert_to_invoice.response.ConvertToInvoiceResponse;
import com.cashly.cashly_api.orders.domain.entity.Order;
import com.cashly.cashly_api.orders.domain.entity.OrderLine;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.application.ports.out.OrderNotificationGateway;
import com.cashly.cashly_api.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.finance.billing.invoices.application.create.from_order.services.OrderInvoiceCreationService;
import com.cashly.cashly_api.orders.infrastructure.integration.LocalInvoiceCreationGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConvertToInvoiceUseCase Unit Tests")
class ConvertToInvoiceUseCaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CreateInvoiceUseCase createInvoiceUseCase;

    @Mock
    private OrderNotificationGateway notificationService;

    @Captor
    private ArgumentCaptor<CreateInvoiceRequest> invoiceRequestCaptor;

    private ConvertToInvoiceUseCase convertToInvoiceUseCase;

    @BeforeEach
    void setUp() {
        convertToInvoiceUseCase = new ConvertToInvoiceUseCase(orderRepository, new LocalInvoiceCreationGateway(new OrderInvoiceCreationService(createInvoiceUseCase)), notificationService);
    }

    @Test
    void should_ConvertConfirmedOrderToInvoice_When_OrderExists() {
        UUID orderId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        Order order = buildConfirmedOrder(orderId);

        InvoiceDetailResponse invoiceResponse = buildInvoiceResponse(orderId);

        when(orderRepository.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));
        when(createInvoiceUseCase.execute(any(CreateInvoiceRequest.class), eq(authenticatedUserId)))
                .thenReturn(invoiceResponse);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        ConvertToInvoiceRequest request = new ConvertToInvoiceRequest("CASH", null, null, null, null);
        ConvertToInvoiceResponse response = convertToInvoiceUseCase.execute(orderId, request, authenticatedUserId);

        assertNotNull(response);
        assertEquals(orderId, response.orderId());
        assertEquals("INVOICED", response.orderStatus());
        assertEquals(invoiceResponse.id(), response.invoiceId());
        assertEquals(invoiceResponse.invoiceNumber(), response.invoiceNumber());
    }

    @Test
    void should_PassPaymentTypeToCreateInvoice_When_Converting() {
        UUID orderId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        Order order = buildConfirmedOrder(orderId);

        InvoiceDetailResponse invoiceResponse = buildInvoiceResponse(orderId);

        when(orderRepository.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));
        when(createInvoiceUseCase.execute(any(CreateInvoiceRequest.class), eq(authenticatedUserId)))
                .thenReturn(invoiceResponse);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        ConvertToInvoiceRequest request = new ConvertToInvoiceRequest("CREDIT", null, null, null, null);
        convertToInvoiceUseCase.execute(orderId, request, authenticatedUserId);

        verify(createInvoiceUseCase).execute(invoiceRequestCaptor.capture(), eq(authenticatedUserId));
        CreateInvoiceRequest captured = invoiceRequestCaptor.getValue();

        assertEquals(orderId, captured.orderId());
        assertEquals("CREDIT", captured.paymentType());
    }

    @Test
    void should_ThrowOrderNotFoundException_When_OrderDoesNotExist() {
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(OrderId.of(orderId))).thenReturn(Optional.empty());

        ConvertToInvoiceRequest request = new ConvertToInvoiceRequest("CASH", null, null, null, null);
        assertThrows(
                OrderNotFoundException.class,
                () -> convertToInvoiceUseCase.execute(orderId, request, UUID.randomUUID())
        );

        verifyNoInteractions(createInvoiceUseCase);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void should_ThrowIllegalStateException_When_OrderIsNotConfirmed() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.reconstitute(
                OrderId.of(orderId), OrderNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "Creator Name",
                OrderStatus.DRAFT, null, null, null, null, null,
                List.of(OrderLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                        BigDecimal.ONE, BigDecimal.TEN, List.of())),
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(orderRepository.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));

        ConvertToInvoiceRequest request = new ConvertToInvoiceRequest("CASH", null, null, null, null);
        assertThrows(
                IllegalStateException.class,
                () -> convertToInvoiceUseCase.execute(orderId, request, UUID.randomUUID())
        );

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(createInvoiceUseCase);
    }

    @Test
    void should_SaveOrderExactlyOnce_When_ConversionSucceeds() {
        UUID orderId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        Order order = buildConfirmedOrder(orderId);

        InvoiceDetailResponse invoiceResponse = buildInvoiceResponse(orderId);

        when(orderRepository.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));
        when(createInvoiceUseCase.execute(any(CreateInvoiceRequest.class), eq(authenticatedUserId)))
                .thenReturn(invoiceResponse);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        ConvertToInvoiceRequest request = new ConvertToInvoiceRequest("CASH", null, null, null, null);
        convertToInvoiceUseCase.execute(orderId, request, authenticatedUserId);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    private InvoiceDetailResponse buildInvoiceResponse(UUID orderId) {
        return new InvoiceDetailResponse(
                UUID.randomUUID(), "INV-2026-00001",
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name",
                orderId, "ORD-2026-00001",
                "PAID", "Pagada", "CASH", null,
                BigDecimal.valueOf(10),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN,
                List.of(),
                null, null,
                LocalDateTime.now(), LocalDateTime.now(),
                List.of(),
                BigDecimal.ZERO, null, List.of()
        );
    }

    private Order buildConfirmedOrder(UUID orderId) {
        OrderLine line = OrderLine.create(
                UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of()
        );
        Order order = Order.reconstitute(
                OrderId.of(orderId), OrderNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "Creator Name",
                OrderStatus.CONFIRMED, null, null, null, null, null,
                List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
        return order;
    }
}
