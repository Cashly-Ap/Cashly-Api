package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.request.UpdateInvoiceRequest;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.shared.application.StockEnrichmentService;
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
@DisplayName("UpdateInvoiceUseCase Unit Tests")
class UpdateInvoiceUseCaseUnitTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private StockEnrichmentService stockEnrichmentService;

    private UpdateInvoiceUseCase updateInvoiceUseCase;

    @BeforeEach
    void setUp() {
        updateInvoiceUseCase = new UpdateInvoiceUseCase(invoiceRepository, productRepository,
                warehouseStockRepository, stockMovementRepository, stockEnrichmentService);
    }

    @Test
    void should_UpdateDraftInvoice_When_RequestIsValid() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT);
        Product product = buildProduct(productId);

        UpdateInvoiceRequest request = new UpdateInvoiceRequest(
                "CASH",
                "Updated notes",
                List.of(new UpdateInvoiceRequest.InvoiceLineRequest(productId, BigDecimal.ONE, BigDecimal.TEN, List.of()))
        );

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockEnrichmentService.enrich(any())).thenReturn(List.of());

        InvoiceDetailResponse response = updateInvoiceUseCase.execute(id, request);

        assertNotNull(response);
        assertEquals("DRAFT", response.status());
        assertEquals(1, response.lines().size());
        verify(invoiceRepository).findById(InvoiceId.of(id));
        verify(productRepository).findById(productId);
        verify(invoiceRepository).save(invoice);
        verify(stockEnrichmentService).enrich(any());
    }

    @Test
    void should_ThrowInvoiceNotFoundException_When_InvoiceDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.empty());

        UpdateInvoiceRequest request = new UpdateInvoiceRequest(
                "CASH", "notes",
                List.of(new UpdateInvoiceRequest.InvoiceLineRequest(
                        UUID.randomUUID(), BigDecimal.ONE, BigDecimal.TEN, List.of()))
        );

        InvoiceNotFoundException exception = assertThrows(
                InvoiceNotFoundException.class,
                () -> updateInvoiceUseCase.execute(id, request)
        );

        assertEquals("Invoice not found with id: " + id, exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_ThrowIllegalStateException_When_InvoiceNotInDraftStatus() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.PAID);
        Product product = buildProduct(productId);

        UpdateInvoiceRequest request = new UpdateInvoiceRequest(
                "CASH", null,
                List.of(new UpdateInvoiceRequest.InvoiceLineRequest(productId, BigDecimal.ONE, BigDecimal.TEN, List.of()))
        );

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> updateInvoiceUseCase.execute(id, request)
        );

        assertTrue(exception.getMessage().contains("DRAFT"));
        verify(invoiceRepository, never()).save(any());
    }

    private Invoice buildInvoice(UUID id, InvoiceStatus status) {
        InvoiceLine line = InvoiceLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Invoice.reconstitute(
                InvoiceId.of(id),
                InvoiceNumber.of(2026, 1),
                UUID.randomUUID(),
                "Customer Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "User Name",
                null,
                null,
                status,
                PaymentType.CASH,
                null,
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Product buildProduct(UUID productId) {
        return Product.reconstitute(
                productId,
                "PROD-001",
                "Product A",
                null,
                null,
                false,
                true,
                null,
                BigDecimal.TEN,
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(0.25),
                List.of(),
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
