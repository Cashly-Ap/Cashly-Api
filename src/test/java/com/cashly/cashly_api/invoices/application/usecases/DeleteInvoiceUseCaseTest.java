package com.cashly.cashly_api.invoices.application.usecases;

import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.entities.InvoiceLine;
import com.cashly.cashly_api.invoices.domain.entities.InvoiceLineAllocation;
import com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.orders.domain.entities.Order;
import com.cashly.cashly_api.orders.domain.entities.OrderLine;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderId;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber;
import com.cashly.cashly_api.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
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
@DisplayName("DeleteInvoiceUseCase Unit Tests")
class DeleteInvoiceUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    private DeleteInvoiceUseCase deleteInvoiceUseCase;

    @BeforeEach
    void setUp() {
        deleteInvoiceUseCase = new DeleteInvoiceUseCase(invoiceRepository,
                new com.cashly.cashly_api.invoices.infrastructure.integration.LocalInvoiceOrderGateway(orderRepository),
                new com.cashly.cashly_api.invoices.infrastructure.integration.LocalInventoryGatewayAdapter(new com.cashly.cashly_api.warehouses.application.services.InvoiceInventoryService(warehouseStockRepository, mock(com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository.class))));
    }

    @Test
    void should_DeleteInvoice_When_InvoiceIsDraft() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT, null);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));

        deleteInvoiceUseCase.execute(id);

        verify(invoiceRepository).deleteById(InvoiceId.of(id));
    }

    @Test
    void should_ThrowInvoiceNotFoundException_When_InvoiceNotFound() {
        UUID id = UUID.randomUUID();

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.empty());

        assertThrows(
                InvoiceNotFoundException.class,
                () -> deleteInvoiceUseCase.execute(id)
        );

        verify(invoiceRepository, never()).deleteById(any());
    }

    @Test
    void should_ThrowException_When_DeletingNonDraftInvoice() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.PAID, null);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));

        assertThrows(
                IllegalStateException.class,
                () -> deleteInvoiceUseCase.execute(id)
        );

        verify(invoiceRepository, never()).deleteById(any());
    }

    @Test
    void should_RevertLinkedOrder_When_InvoiceHasOrderId() {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT, orderId);
        Order order = buildOrder(orderId);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(orderRepository.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));

        deleteInvoiceUseCase.execute(id);

        verify(orderRepository).save(order);
        verify(invoiceRepository).deleteById(InvoiceId.of(id));
    }

    @Test
    void should_RevertStockAllocations_When_InvoiceHasAllocations() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        InvoiceLineAllocation allocation = InvoiceLineAllocation.create(warehouseId, BigDecimal.valueOf(3));
        InvoiceLine line = InvoiceLine.create(productId, "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));
        Invoice invoice = Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "User", null, null,
                InvoiceStatus.DRAFT, PaymentType.CASH, null,
                BigDecimal.ZERO, null, null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );

        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId, 50, 3,
                LocalDateTime.now(), LocalDateTime.now());

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));

        deleteInvoiceUseCase.execute(id);

        verify(warehouseStockRepository).save(stock);
    }

    private Invoice buildInvoice(UUID id, InvoiceStatus status, UUID orderId) {
        InvoiceLine line = InvoiceLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name", orderId, null,
                status, PaymentType.CASH, null,
                BigDecimal.ZERO, null, null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Order buildOrder(UUID orderId) {
        OrderLine line = OrderLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Order.reconstitute(
                OrderId.of(orderId), OrderNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name",
                OrderStatus.INVOICED, null, null, null, null, null,
                List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
