package com.cashly.cashly_api.sales.orders.reporting.application.generate_order_pdf.services;

import com.cashly.cashly_api.sales.orders.reporting.application.dto.model.OrderReportData;
import com.cashly.cashly_api.sales.orders.application.mappers.OrderMapper;
import com.cashly.cashly_api.sales.orders.domain.entity.Order;
import com.cashly.cashly_api.sales.orders.domain.entity.OrderLine;
import com.cashly.cashly_api.sales.orders.domain.entity.OrderLineAllocation;
import com.cashly.cashly_api.sales.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderLineId;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.sales.orders.reporting.application.ports.out.OrderPdfPort;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import com.cashly.cashly_api.sales.orders.application.queries.services.OrderDocumentQueryService;
import com.cashly.cashly_api.sales.orders.application.list.use_case.GetOrdersUseCase;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.sales.orders.reporting.infrastructure.integration.OrderDocumentQueryAdapter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateOrderPdfUseCaseImpl Unit Tests")
class GenerateOrderPdfUseCaseImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private OrderPdfPort orderPdfPort;

    private GenerateOrderPdfUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateOrderPdfUseCaseImpl(
                new OrderDocumentQueryAdapter(new OrderDocumentQueryService(
                        orderRepository,
                        new GetOrdersUseCase(orderRepository,
                                mock(OrderProductCatalogGateway.class)))),
                orderPdfPort);
    }

    @Test
    @DisplayName("should generate order PDF for given order ID")
    void should_generate_order_pdf() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(OrderId.of(orderId));
        when(order.getOrderNumber()).thenReturn(new OrderNumber("ORD-001"));
        when(order.getCustomerId()).thenReturn(UUID.randomUUID());
        when(order.getCustomerName()).thenReturn("Customer A");
        when(order.getStatus()).thenReturn(OrderStatus.CONFIRMED);
        when(order.getSubtotal()).thenReturn(BigDecimal.valueOf(100));
        when(order.getInvoiceId()).thenReturn(null);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.now());

        OrderLine line = mock(OrderLine.class);
        OrderLineId lineId = OrderLineId.generate();
        when(line.getId()).thenReturn(lineId);
        when(line.getProductId()).thenReturn(UUID.randomUUID());
        when(line.getProductName()).thenReturn("Product A");
        when(line.getProductCode()).thenReturn("SKU001");
        when(line.getQuantity()).thenReturn(BigDecimal.valueOf(2));
        when(line.getUnitPrice()).thenReturn(BigDecimal.valueOf(50));
        when(line.getLineTotal()).thenReturn(BigDecimal.valueOf(100));

        OrderLineAllocation allocation = mock(OrderLineAllocation.class);
        UUID warehouseId = UUID.randomUUID();
        when(allocation.getWarehouseId()).thenReturn(warehouseId);
        when(allocation.getAllocated()).thenReturn(BigDecimal.valueOf(2));
        when(line.getAllocations()).thenReturn(List.of(allocation));

        when(order.getLines()).thenReturn(List.of(line));
        when(orderRepository.findById(OrderId.of(orderId)))
                .thenReturn(Optional.of(order));

        byte[] expectedPdf = "Order PDF".getBytes();
        when(orderPdfPort.generatePdf(any(OrderReportData.class)))
                .thenReturn(expectedPdf);

        byte[] result = useCase.execute(orderId);

        assertArrayEquals(expectedPdf, result);
        verify(orderRepository).findById(OrderId.of(orderId));
        verify(orderPdfPort).generatePdf(any(OrderReportData.class));
        var projection = org.mockito.ArgumentCaptor
                .forClass(OrderReportData.class);
        verify(orderPdfPort).generatePdf(projection.capture());
        assertEquals("ORD-001", projection.getValue().orderNumber());
        assertEquals("Customer A", projection.getValue().customerName());
        assertEquals(BigDecimal.valueOf(100), projection.getValue().subtotal());
        assertEquals(BigDecimal.valueOf(2),
                projection.getValue().lines().getFirst().quantity());
        assertEquals(BigDecimal.valueOf(50),
                projection.getValue().lines().getFirst().unitPrice());
        assertEquals(BigDecimal.valueOf(100),
                projection.getValue().lines().getFirst().lineTotal());
        assertThrows(UnsupportedOperationException.class,
                () -> projection.getValue().lines().clear());
    }

    @Test
    @DisplayName("should throw OrderNotFoundException when order not found")
    void should_throw_when_order_not_found() {
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(OrderId.of(orderId)))
                .thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> useCase.execute(orderId));
        verify(orderRepository).findById(OrderId.of(orderId));
        verifyNoInteractions(orderPdfPort);
    }

    @Test
    @DisplayName("should generate order PDF without allocations")
    void should_generate_order_pdf_without_allocations() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(OrderId.of(orderId));
        when(order.getOrderNumber()).thenReturn(new OrderNumber("ORD-002"));
        when(order.getCustomerId()).thenReturn(UUID.randomUUID());
        when(order.getCustomerName()).thenReturn("Customer B");
        when(order.getStatus()).thenReturn(OrderStatus.DRAFT);
        when(order.getSubtotal()).thenReturn(BigDecimal.valueOf(200));
        when(order.getInvoiceId()).thenReturn(null);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.now());

        OrderLine line = mock(OrderLine.class);
        OrderLineId lineId = OrderLineId.generate();
        when(line.getId()).thenReturn(lineId);
        when(line.getProductId()).thenReturn(UUID.randomUUID());
        when(line.getProductName()).thenReturn("Product B");
        when(line.getProductCode()).thenReturn("SKU002");
        when(line.getQuantity()).thenReturn(BigDecimal.valueOf(1));
        when(line.getUnitPrice()).thenReturn(BigDecimal.valueOf(200));
        when(line.getLineTotal()).thenReturn(BigDecimal.valueOf(200));
        when(line.getAllocations()).thenReturn(List.of());

        when(order.getLines()).thenReturn(List.of(line));
        when(orderRepository.findById(OrderId.of(orderId)))
                .thenReturn(Optional.of(order));

        byte[] expectedPdf = "Order no allocation PDF".getBytes();
        when(orderPdfPort.generatePdf(any(OrderReportData.class)))
                .thenReturn(expectedPdf);

        byte[] result = useCase.execute(orderId);

        assertArrayEquals(expectedPdf, result);
        verify(orderRepository).findById(OrderId.of(orderId));
        verify(orderPdfPort).generatePdf(any(OrderReportData.class));
    }
}
