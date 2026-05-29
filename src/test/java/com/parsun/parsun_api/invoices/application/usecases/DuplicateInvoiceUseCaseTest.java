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
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DuplicateInvoiceUseCase Unit Tests")
class DuplicateInvoiceUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private ProductRepository productRepository;

    private DuplicateInvoiceUseCase duplicateInvoiceUseCase;

    @BeforeEach
    void setUp() {
        duplicateInvoiceUseCase = new DuplicateInvoiceUseCase(invoiceRepository,
                warehouseStockRepository, productRepository);
    }

    @Test
    void should_DuplicateInvoice_When_InvoiceExists() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Invoice original = buildInvoice(id, productId);
        Product product = buildProduct(productId);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(original));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(2);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDetailResponse response = duplicateInvoiceUseCase.execute(id);

        assertNotNull(response);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void should_ThrowInvoiceNotFoundException_When_InvoiceNotFound() {
        UUID id = UUID.randomUUID();

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.empty());

        assertThrows(
                InvoiceNotFoundException.class,
                () -> duplicateInvoiceUseCase.execute(id)
        );

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_GenerateNewInvoiceNumber_When_Duplicating() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Invoice original = buildInvoice(id, productId);
        Product product = buildProduct(productId);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(original));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(99);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        duplicateInvoiceUseCase.execute(id);

        verify(invoiceRepository).getNextSequenceForYear(anyInt());
    }

    @Test
    void should_DuplicateWithStockAllocations_When_OriginalHasAllocations() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        InvoiceLineAllocation allocation = InvoiceLineAllocation.create(warehouseId, BigDecimal.valueOf(5));
        InvoiceLine line = InvoiceLine.create(productId, "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of(allocation));
        Invoice original = Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "User", null, null,
                InvoiceStatus.DRAFT, PaymentType.CASH, null,
                BigDecimal.ZERO, null, null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );

        Product product = buildProduct(productId);
        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId, 50, 0,
                LocalDateTime.now(), LocalDateTime.now());

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(original));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(2);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        duplicateInvoiceUseCase.execute(id);

        assertEquals(5, stock.getReserved());
        verify(warehouseStockRepository).save(stock);
    }

    @Test
    void should_ThrowException_When_ProductNotFound() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Invoice original = buildInvoice(id, productId);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(original));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(2);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> duplicateInvoiceUseCase.execute(id)
        );

        verify(invoiceRepository).getNextSequenceForYear(anyInt());
        verify(invoiceRepository, never()).save(any());
    }

    private Invoice buildInvoice(UUID id, UUID productId) {
        InvoiceLine line = InvoiceLine.create(productId, "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name", null, null,
                InvoiceStatus.DRAFT, PaymentType.CASH, null,
                BigDecimal.ZERO, null, null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Product buildProduct(UUID productId) {
        return Product.reconstitute(
                productId, "PROD-001", "Product A",
                null, null, false, true, null,
                BigDecimal.TEN, BigDecimal.valueOf(8), BigDecimal.valueOf(0.25),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
