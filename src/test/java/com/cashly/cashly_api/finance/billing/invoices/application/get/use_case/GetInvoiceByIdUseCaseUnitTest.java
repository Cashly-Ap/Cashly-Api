package com.cashly.cashly_api.finance.billing.invoices.application.get.use_case;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceNoteDetailsGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceReturnDetailsGateway;
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
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.StockAvailabilityGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetInvoiceByIdUseCase Unit Tests")
class GetInvoiceByIdUseCaseUnitTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private StockAvailabilityGateway stockEnrichmentService;

    @Mock
    private InvoiceNoteDetailsGateway noteDetails;

    @Mock
    private InvoiceReturnDetailsGateway returnDetails;

    private GetInvoiceByIdUseCase getInvoiceByIdUseCase;

    @BeforeEach
    void setUp() {
        getInvoiceByIdUseCase = new GetInvoiceByIdUseCase(invoiceRepository,
                stockEnrichmentService, noteDetails, returnDetails);
        lenient().when(noteDetails.findCreditNotes(any()))
                .thenReturn(List.of());
        lenient().when(noteDetails.findDebitNotes(any())).thenReturn(List.of());
        lenient().when(returnDetails.findByInvoiceId(any()))
                .thenReturn(List.of());
        lenient().when(
                stockEnrichmentService.getAllStockByProducts(any(), any()))
                .thenReturn(java.util.Map.of());
    }

    @Test
    void should_ReturnInvoiceDetail_When_InvoiceExists() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Invoice invoice = buildInvoiceWithAllocations(id, warehouseId,
                productId);

        when(invoiceRepository.findById(InvoiceId.of(id)))
                .thenReturn(Optional.of(invoice));
        when(stockEnrichmentService.enrich(any())).thenReturn(
                List.of(new StockAvailabilityGateway.EnrichedStock(warehouseId,
                        productId, "WH1", "Warehouse 1", 15)));

        InvoiceDetailResponse response = getInvoiceByIdUseCase.execute(id);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().get(0).warehouseAllocations()).hasSize(1);
        assertThat(response.lines().get(0).warehouseAllocations().get(0)
                .available()).isEqualTo(15);
        verify(invoiceRepository).findById(InvoiceId.of(id));
        verify(stockEnrichmentService).enrich(any());
    }

    @Test
    void should_ThrowInvoiceNotFoundException_When_InvoiceDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(invoiceRepository.findById(InvoiceId.of(id)))
                .thenReturn(Optional.empty());

        InvoiceNotFoundException exception = assertThrows(
                InvoiceNotFoundException.class,
                () -> getInvoiceByIdUseCase.execute(id));

        assertEquals("Invoice not found with id: " + id,
                exception.getMessage());
        verify(invoiceRepository).findById(InvoiceId.of(id));
        verifyNoInteractions(stockEnrichmentService);
    }

    @Test
    void should_NeverSave_When_Querying() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoiceWithAllocations(id);

        when(invoiceRepository.findById(any()))
                .thenReturn(Optional.of(invoice));
        when(stockEnrichmentService.enrich(any())).thenReturn(List.of());

        getInvoiceByIdUseCase.execute(id);

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_ReturnCorrectAvailableStock_When_EnrichmentProvidesData() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Invoice invoice = buildInvoiceWithAllocations(id, warehouseId,
                productId);

        when(invoiceRepository.findById(InvoiceId.of(id)))
                .thenReturn(Optional.of(invoice));
        when(stockEnrichmentService.enrich(any())).thenReturn(
                List.of(new StockAvailabilityGateway.EnrichedStock(warehouseId,
                        productId, "WH-MAIN", "Deposito Principal", 25)));

        InvoiceDetailResponse response = getInvoiceByIdUseCase.execute(id);

        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().get(0).warehouseAllocations()).hasSize(1);
        InvoiceDetailResponse.WarehouseAllocationResponse allocation = response
                .lines().get(0).warehouseAllocations().get(0);
        assertThat(allocation.warehouseId()).isEqualTo(warehouseId);
        assertThat(allocation.warehouseCode()).isEqualTo("WH-MAIN");
        assertThat(allocation.warehouseName()).isEqualTo("Deposito Principal");
        assertThat(allocation.available()).isEqualTo(25);
    }

    @Test
    void should_ReturnZeroAvailableStock_When_LineHasNoEnrichment() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoiceWithAllocations(id);

        when(invoiceRepository.findById(InvoiceId.of(id)))
                .thenReturn(Optional.of(invoice));
        when(stockEnrichmentService.enrich(any())).thenReturn(List.of());

        InvoiceDetailResponse response = getInvoiceByIdUseCase.execute(id);

        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().get(0).warehouseAllocations()).hasSize(1);
        assertThat(response.lines().get(0).warehouseAllocations().get(0)
                .available()).isZero();
    }

    private Invoice buildInvoiceWithAllocations(UUID id) {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        return buildInvoiceWithAllocations(id, warehouseId, productId);
    }

    private Invoice buildInvoiceWithAllocations(UUID id, UUID warehouseId,
            UUID productId) {
        InvoiceLineAllocation allocation = InvoiceLineAllocation
                .create(warehouseId, BigDecimal.valueOf(3));
        InvoiceLine line = InvoiceLine.create(productId, "Product A",
                "PROD-001", BigDecimal.ONE, BigDecimal.TEN,
                List.of(allocation));
        return Invoice.reconstitute(InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                UUID.randomUUID(), "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name", null, null, InvoiceStatus.DRAFT,
                PaymentType.CASH, null, BigDecimal.ZERO, null, null, null,
                List.of(line), LocalDateTime.now(), LocalDateTime.now());
    }
}
