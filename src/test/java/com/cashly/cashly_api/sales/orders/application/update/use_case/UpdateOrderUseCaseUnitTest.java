package com.cashly.cashly_api.sales.orders.application.update.use_case;

import com.cashly.cashly_api.sales.orders.application.update.request.UpdateOrderRequest;
import com.cashly.cashly_api.sales.orders.application.dto.response.OrderDetailResponse;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.sales.orders.domain.entity.Order;
import com.cashly.cashly_api.sales.orders.domain.entity.OrderLine;
import com.cashly.cashly_api.sales.orders.domain.entity.OrderLineAllocation;
import com.cashly.cashly_api.sales.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateOrderUseCase Unit Tests")
class UpdateOrderUseCaseUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProductCatalogGateway productRepository;

    @Mock
    private OrderInventoryGateway warehouseStockRepository;

    private UpdateOrderUseCase updateOrderUseCase;

    @BeforeEach
    void setUp() {
        updateOrderUseCase = new UpdateOrderUseCase(orderRepository,
                productRepository, warehouseStockRepository);
    }

    @Test
    void should_UpdateDraftOrder_When_OrderExists() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderLineAllocation oldAllocation = OrderLineAllocation.create(warehouseId, BigDecimal.valueOf(3));
        OrderLine oldLine = OrderLine.create(productId, "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of(oldAllocation));
        Order order = Order.reconstitute(
                OrderId.of(id), OrderNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "Creator Name",
                OrderStatus.DRAFT, "Old notes", null, null, null, null,
                List.of(oldLine), LocalDateTime.now(), LocalDateTime.now()
        );

        Product product = buildProduct(productId);
        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId, 50, 5,
                LocalDateTime.now(), LocalDateTime.now());

        UpdateOrderRequest request = new UpdateOrderRequest(
                "New notes",
                List.of(new UpdateOrderRequest.OrderLineRequest(
                        productId, BigDecimal.valueOf(2), BigDecimal.valueOf(15),
                        List.of(new UpdateOrderRequest.WarehouseAllocationRequest(warehouseId, BigDecimal.valueOf(2)))
                ))
        );

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(productRepository.findProduct(productId)).thenReturn(Optional.of(productRef(product)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderDetailResponse response = updateOrderUseCase.execute(id, request);

        assertNotNull(response);
        assertEquals("DRAFT", response.status());
        verify(orderRepository).findById(OrderId.of(id));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void should_ThrowOrderNotFoundException_When_OrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        UpdateOrderRequest request = new UpdateOrderRequest("notes", List.of());

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> updateOrderUseCase.execute(id, request)
        );

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(productRepository, warehouseStockRepository);
    }

    @Test
    void should_ThrowIllegalStateException_When_OrderIsNotDraft() {
        UUID id = UUID.randomUUID();
        Order order = Order.reconstitute(
                OrderId.of(id), OrderNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "Creator Name",
                OrderStatus.CONFIRMED, null, null, null, null, null,
                List.of(OrderLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                        BigDecimal.ONE, BigDecimal.TEN, List.of())),
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));

        UpdateOrderRequest request = new UpdateOrderRequest("notes", List.of());

        assertThrows(
                IllegalStateException.class,
                () -> updateOrderUseCase.execute(id, request)
        );

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(productRepository, warehouseStockRepository);
    }

    @Test
    void should_ReleaseOldReservationsAndReserveNew_When_Updating() {
        UUID id = UUID.randomUUID();
        UUID oldWarehouseId = UUID.randomUUID();
        UUID newWarehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderLineAllocation oldAllocation = OrderLineAllocation.create(oldWarehouseId, BigDecimal.valueOf(3));
        OrderLine oldLine = OrderLine.create(productId, "Old Product", "OLD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of(oldAllocation));
        Order order = Order.reconstitute(
                OrderId.of(id), OrderNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "Creator Name",
                OrderStatus.DRAFT, null, null, null, null, null,
                List.of(oldLine), LocalDateTime.now(), LocalDateTime.now()
        );

        Product product = buildProduct(productId);
        WarehouseStock oldStock = new WarehouseStock(UUID.randomUUID(), oldWarehouseId, productId, 50, 5,
                LocalDateTime.now(), LocalDateTime.now());
        WarehouseStock newStock = new WarehouseStock(UUID.randomUUID(), newWarehouseId, productId, 30, 0,
                LocalDateTime.now(), LocalDateTime.now());

        UpdateOrderRequest request = new UpdateOrderRequest(
                null,
                List.of(new UpdateOrderRequest.OrderLineRequest(
                        productId, BigDecimal.valueOf(2), BigDecimal.valueOf(15),
                        List.of(new UpdateOrderRequest.WarehouseAllocationRequest(newWarehouseId, BigDecimal.valueOf(2)))
                ))
        );

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(productRepository.findProduct(productId)).thenReturn(Optional.of(productRef(product)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        updateOrderUseCase.execute(id, request);

        
        verify(warehouseStockRepository).release(List.of(new OrderInventoryGateway.Allocation(oldWarehouseId,
                productId, 3)));
        verify(warehouseStockRepository).reserve(List.of(new OrderInventoryGateway.Allocation(newWarehouseId,
                productId, 2)));
    }

    @Test
    void should_ThrowIllegalArgumentException_When_ProductNotFound() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderLine line = OrderLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        Order order = Order.reconstitute(
                OrderId.of(id), OrderNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "Creator Name",
                OrderStatus.DRAFT, null, null, null, null, null,
                List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );

        UpdateOrderRequest request = new UpdateOrderRequest(
                "notes",
                List.of(new UpdateOrderRequest.OrderLineRequest(
                        productId, BigDecimal.ONE, BigDecimal.TEN, List.of()
                ))
        );

        when(orderRepository.findById(OrderId.of(id))).thenReturn(Optional.of(order));
        when(productRepository.findProduct(productId)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> updateOrderUseCase.execute(id, request)
        );

        verify(orderRepository, never()).save(any());
    }

    private Product buildProduct(UUID productId) {
        return Product.reconstitute(
                productId, "PROD-001", "Product A",
                null, null, false, true, null,
                BigDecimal.valueOf(8),
                List.of(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private OrderProductCatalogGateway.ProductReference productRef(Product product) {
        return new OrderProductCatalogGateway.ProductReference(product.getId(), product.getCode(), product.getName(),
                product.getCategoryId());
    }
}
