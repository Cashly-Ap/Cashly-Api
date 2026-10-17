package com.cashly.cashly_api.finance.billing.invoices.application.update.use_case;

import com.cashly.cashly_api.finance.billing.invoices.application.update.request.UpdateInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLineAllocation;
import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.StockAvailabilityGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
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
class UpdateInvoiceUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private StockAvailabilityGateway stockEnrichmentService;

    private UpdateInvoiceUseCase updateInvoiceUseCase;

    @BeforeEach
    void setUp() {
        updateInvoiceUseCase = new UpdateInvoiceUseCase(invoiceRepository,
                new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInvoiceCatalogGateway(new com.cashly.cashly_api.catalog.products.application.queries.services.RepositoryStockCatalogQuery(productRepository)), new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInventoryGatewayAdapter(new com.cashly.cashly_api.inventory.warehouses.stock.application.invoices.services.InvoiceInventoryService(warehouseStockRepository, mock(com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository.class))), stockEnrichmentService, new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalCustomerCreditTermsGateway(new com.cashly.cashly_api.partners.customers.profile.application.queries.services.RepositoryCustomerCreditTermsQuery(mock(com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository.class))));
    }

    @Test
    void should_UpdateInvoice_When_InvoiceIsDraft() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT);
        Product product = buildProduct(productId);

        UpdateInvoiceRequest.InvoiceLineRequest lineRequest = new UpdateInvoiceRequest.InvoiceLineRequest(
                productId, BigDecimal.TWO, BigDecimal.valueOf(15.00), List.of()
        );
        UpdateInvoiceRequest request = new UpdateInvoiceRequest("CASH", "Updated notes", List.of(lineRequest));

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockEnrichmentService.enrich(any())).thenReturn(List.of());

        InvoiceDetailResponse response = updateInvoiceUseCase.execute(id, request);

        assertNotNull(response);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void should_ThrowInvoiceNotFoundException_When_InvoiceNotFound() {
        UUID id = UUID.randomUUID();

        UpdateInvoiceRequest request = new UpdateInvoiceRequest("CASH", "Notes", List.of());

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.empty());

        assertThrows(
                InvoiceNotFoundException.class,
                () -> updateInvoiceUseCase.execute(id, request)
        );

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_ThrowException_When_UpdatingNonDraftInvoice() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.PAID);
        Product product = buildProduct(productId);

        UpdateInvoiceRequest.InvoiceLineRequest lineRequest = new UpdateInvoiceRequest.InvoiceLineRequest(
                productId, BigDecimal.ONE, BigDecimal.TEN, List.of()
        );
        UpdateInvoiceRequest request = new UpdateInvoiceRequest("CASH", "Notes", List.of(lineRequest));

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(
                IllegalStateException.class,
                () -> updateInvoiceUseCase.execute(id, request)
        );

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_RevertStockAllocations_When_Updating() {
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

        Product product = buildProduct(productId);
        UpdateInvoiceRequest.InvoiceLineRequest lineRequest = new UpdateInvoiceRequest.InvoiceLineRequest(
                productId, BigDecimal.ONE, BigDecimal.TEN, List.of()
        );
        UpdateInvoiceRequest request = new UpdateInvoiceRequest("CASH", "Notes", List.of(lineRequest));

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockEnrichmentService.enrich(any())).thenReturn(List.of());

        updateInvoiceUseCase.execute(id, request);

        verify(warehouseStockRepository).save(stock);
    }

    private Invoice buildInvoice(UUID id, InvoiceStatus status) {
        InvoiceLine line = InvoiceLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name", null, null,
                status, PaymentType.CASH, null,
                BigDecimal.ZERO, null, null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Product buildProduct(UUID productId) {
        return Product.reconstitute(
                productId, "PROD-001", "Product A",
                null, null, false, true, null,
                BigDecimal.valueOf(8),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
