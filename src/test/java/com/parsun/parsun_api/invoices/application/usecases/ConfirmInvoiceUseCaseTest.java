package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
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
@DisplayName("ConfirmInvoiceUseCase Unit Tests")
class ConfirmInvoiceUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    private ConfirmInvoiceUseCase confirmInvoiceUseCase;

    @BeforeEach
    void setUp() {
        confirmInvoiceUseCase = new ConfirmInvoiceUseCase(invoiceRepository,
                warehouseStockRepository, stockMovementRepository);
    }

    @Test
    void should_ConfirmInvoice_When_InvoiceIsDraftAndCash() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT, PaymentType.CASH);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDetailResponse response = confirmInvoiceUseCase.execute(id);

        assertNotNull(response);
        assertEquals("PAID", response.status());
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void should_ConfirmInvoice_When_InvoiceIsDraftAndCredit() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT, PaymentType.CREDIT);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDetailResponse response = confirmInvoiceUseCase.execute(id);

        assertNotNull(response);
        assertEquals("PENDING_PAYMENT", response.status());
    }

    @Test
    void should_ThrowInvoiceNotFoundException_When_InvoiceNotFound() {
        UUID id = UUID.randomUUID();

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.empty());

        InvoiceNotFoundException exception = assertThrows(
                InvoiceNotFoundException.class,
                () -> confirmInvoiceUseCase.execute(id)
        );

        assertEquals("Invoice not found with id: " + id, exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_ThrowException_When_InvoiceNotInDraftStatus() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.PAID, PaymentType.CASH);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));

        assertThrows(
                IllegalStateException.class,
                () -> confirmInvoiceUseCase.execute(id)
        );

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_DeductStockAllocations_When_InvoiceHasAllocations() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int allocatedQty = 5;

        InvoiceLineAllocation allocation = InvoiceLineAllocation.create(warehouseId, BigDecimal.valueOf(allocatedQty));
        InvoiceLine line = InvoiceLine.create(productId, "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));
        Invoice invoice = Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "User", null, null,
                InvoiceStatus.DRAFT, PaymentType.CASH, null,
                BigDecimal.ZERO, null, null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );

        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId, 50, 5,
                LocalDateTime.now(), LocalDateTime.now());

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        confirmInvoiceUseCase.execute(id);

        assertEquals(0, stock.getReserved());  // 5 - 5 = 0
        verify(warehouseStockRepository).save(stock);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    private Invoice buildInvoice(UUID id, InvoiceStatus status, PaymentType paymentType) {
        InvoiceLine line = InvoiceLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name", null, null,
                status, paymentType, null,
                BigDecimal.ZERO, null, null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
