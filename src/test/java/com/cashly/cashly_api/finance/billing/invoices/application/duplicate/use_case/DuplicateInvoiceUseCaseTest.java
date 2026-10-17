package com.cashly.cashly_api.finance.billing.invoices.application.duplicate.use_case;

import com.cashly.cashly_api.customers.profile.domain.entity.Customer;
import com.cashly.cashly_api.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CreditDays;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CreditLimit;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerAlias;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerCode;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.CustomerType;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.LegalName;
import com.cashly.cashly_api.customers.profile.domain.valueobjects.Rif;
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
import com.cashly.cashly_api.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.repository.WarehouseStockRepository;
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

    @Mock
    private CustomerRepository customerRepository;

    @Captor
    private ArgumentCaptor<Invoice> invoiceCaptor;

    private DuplicateInvoiceUseCase duplicateInvoiceUseCase;

    @BeforeEach
    void setUp() {
        duplicateInvoiceUseCase = new DuplicateInvoiceUseCase(invoiceRepository,
                new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInventoryGatewayAdapter(new com.cashly.cashly_api.warehouses.stock.application.invoices.services.InvoiceInventoryService(warehouseStockRepository, mock(com.cashly.cashly_api.warehouses.stock.domain.repository.StockMovementRepository.class))), new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInvoiceCatalogGateway(new com.cashly.cashly_api.catalog.products.application.queries.services.RepositoryStockCatalogQuery(productRepository)), new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInvoiceCustomerGateway(new com.cashly.cashly_api.customers.profile.application.queries.services.RepositoryInvoiceCustomerQuery(customerRepository)));
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

        InvoiceDetailResponse response = duplicateInvoiceUseCase.execute(id, null);

        assertNotNull(response);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void should_ThrowInvoiceNotFoundException_When_InvoiceNotFound() {
        UUID id = UUID.randomUUID();

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.empty());

        assertThrows(
                InvoiceNotFoundException.class,
                () -> duplicateInvoiceUseCase.execute(id, null)
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

        duplicateInvoiceUseCase.execute(id, null);

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
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(2);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        duplicateInvoiceUseCase.execute(id, null);

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
                () -> duplicateInvoiceUseCase.execute(id, null)
        );

        verify(invoiceRepository).getNextSequenceForYear(anyInt());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_DuplicateWithOriginalCustomer_When_NewCustomerIdIsNull() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID originalCustomerId = UUID.randomUUID();
        InvoiceLine line = InvoiceLine.create(productId, "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        Invoice original = Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                originalCustomerId, "Original Customer", "J-11111111-1",
                UUID.randomUUID(), "User Name", null, null,
                InvoiceStatus.DRAFT, PaymentType.CASH, null,
                BigDecimal.ZERO, null, null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
        Product product = buildProduct(productId);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(original));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(2);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        duplicateInvoiceUseCase.execute(id, null);

        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice saved = invoiceCaptor.getValue();
        assertEquals(originalCustomerId, saved.getCustomerId());
        assertEquals("Original Customer", saved.getCustomerName());
        assertEquals("J-11111111-1", saved.getCustomerRif());
        verifyNoInteractions(customerRepository);
    }

    @Test
    void should_DuplicateWithNewCustomer_When_NewCustomerIdIsProvided() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID newCustomerUuid = UUID.randomUUID();
        Invoice original = buildInvoice(id, productId);
        Product product = buildProduct(productId);

        Customer newCustomer = Customer.reconstitute(
                new CustomerId(newCustomerUuid), new CustomerCode(100L),
                new LegalName("New Customer SA"),
                CustomerType.BUSINESS, new Rif("J-22222222-2"),
                new CustomerAlias("NewCo"), new CreditDays(30),
                new CreditLimit(BigDecimal.valueOf(50000)),
                true, LocalDateTime.now(), LocalDateTime.now(),
                List.of(), List.of(), List.of(),
                null, null
        );

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(original));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(customerRepository.findById(new CustomerId(newCustomerUuid)))
                .thenReturn(Optional.of(newCustomer));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(2);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        duplicateInvoiceUseCase.execute(id, newCustomerUuid);

        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice saved = invoiceCaptor.getValue();
        assertEquals(newCustomerUuid, saved.getCustomerId());
        assertEquals("New Customer SA", saved.getCustomerName());
        assertEquals("J-22222222-2", saved.getCustomerRif());
    }

    @Test
    void should_ThrowException_When_NewCustomerNotFound() {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID nonExistentCustomerId = UUID.randomUUID();
        Invoice original = buildInvoice(id, productId);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(original));
        when(customerRepository.findById(new CustomerId(nonExistentCustomerId)))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> duplicateInvoiceUseCase.execute(id, nonExistentCustomerId)
        );

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
                BigDecimal.valueOf(8),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
