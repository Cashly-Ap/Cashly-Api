package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderId;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderNumber;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderStatus;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteInvoiceUseCase Unit Tests")
class DeleteInvoiceUseCaseUnitTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Captor
    private ArgumentCaptor<StockMovement> stockMovementCaptor;

    private DeleteInvoiceUseCase deleteInvoiceUseCase;

    @BeforeEach
    void setUp() {
        deleteInvoiceUseCase = new DeleteInvoiceUseCase(
                invoiceRepository, orderRepository,
                warehouseStockRepository, stockMovementRepository);
    }

    @Test
    void should_DeleteDraftInvoice_When_InvoiceExists() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildDraftInvoice(id, null);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));

        deleteInvoiceUseCase.execute(id);

        verify(invoiceRepository).findById(InvoiceId.of(id));
        verify(invoiceRepository).deleteById(InvoiceId.of(id));
    }

    @Test
    void should_ThrowInvoiceNotFoundException_When_InvoiceDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.empty());

        assertThrows(
                InvoiceNotFoundException.class,
                () -> deleteInvoiceUseCase.execute(id)
        );

        verify(invoiceRepository, never()).deleteById(any());
        verifyNoInteractions(orderRepository, warehouseStockRepository, stockMovementRepository);
    }

    @Test
    void should_ThrowIllegalStateException_When_InvoiceIsNotDraft() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.PAID, PaymentType.CASH, null);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));

        assertThrows(
                IllegalStateException.class,
                () -> deleteInvoiceUseCase.execute(id)
        );

        verify(invoiceRepository, never()).deleteById(any());
        verifyNoInteractions(orderRepository, warehouseStockRepository, stockMovementRepository);
    }

    @Test
    void should_RevertLinkedOrder_When_InvoiceHasOrderReference() {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Invoice invoice = buildDraftInvoice(id, orderId);
        Order order = Order.reconstitute(
                OrderId.of(orderId), OrderNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "Creator Name",
                OrderStatus.INVOICED, null,
                id, "INV-2026-00001",
                null, null,
                List.of(OrderLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                        BigDecimal.ONE, BigDecimal.TEN, List.of())),
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(orderRepository.findById(OrderId.of(orderId))).thenReturn(Optional.of(order));

        deleteInvoiceUseCase.execute(id);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertNull(order.getInvoiceId());
        assertNull(order.getInvoiceNumber());
        verify(orderRepository).save(order);
        verify(invoiceRepository).deleteById(InvoiceId.of(id));
    }

    @Test
    void should_NotRevertOrder_When_InvoiceHasNoOrderReference() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildDraftInvoice(id, null);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));

        deleteInvoiceUseCase.execute(id);

        verifyNoInteractions(orderRepository);
        verify(invoiceRepository).deleteById(InvoiceId.of(id));
    }

    @Test
    void should_RestoreStockAndCreateMovement_When_DeletingInvoice() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        InvoiceLineAllocation allocation = InvoiceLineAllocation.create(warehouseId, BigDecimal.valueOf(5));
        InvoiceLine line = InvoiceLine.create(productId, "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));
        Invoice invoice = Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name",
                null, null,
                InvoiceStatus.DRAFT, PaymentType.CASH, null,
                null, null,
                List.of(line),
                LocalDateTime.now(), LocalDateTime.now()
        );

        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId, 45, 0,
                LocalDateTime.now(), LocalDateTime.now());

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));

        deleteInvoiceUseCase.execute(id);

        assertEquals(50, stock.getQuantity());  // 45 + 5
        verify(warehouseStockRepository).save(stock);

        verify(stockMovementRepository).save(stockMovementCaptor.capture());
        StockMovement movement = stockMovementCaptor.getValue();
        assertEquals(MovementType.ENTRY, movement.getMovementType());
        assertEquals(5, movement.getQuantity());
        assertEquals(45, movement.getPreviousStock());
        assertEquals(50, movement.getNewStock());
        assertEquals("Invoice deleted", movement.getNotes());

        verify(invoiceRepository).deleteById(InvoiceId.of(id));
    }

    private Invoice buildDraftInvoice(UUID id, UUID orderId) {
        InvoiceLine line = InvoiceLine.create(
                UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return buildInvoice(id, InvoiceStatus.DRAFT, PaymentType.CASH, orderId, line);
    }

    private Invoice buildInvoice(UUID id, InvoiceStatus status, PaymentType paymentType,
                                  UUID orderId, InvoiceLine... lines) {
        List<InvoiceLine> lineList = lines.length > 0 ? List.of(lines) :
                List.of(InvoiceLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                        BigDecimal.ONE, BigDecimal.TEN, List.of()));
        return Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name",
                orderId, null,
                status, paymentType, null,
                null, null,
                lineList,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
