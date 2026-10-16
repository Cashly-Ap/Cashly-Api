package com.cashly.cashly_api.returns.application.delete.use_case;

import com.cashly.cashly_api.returns.application.get.use_case.GetReturnByIdUseCase;

import com.cashly.cashly_api.returns.infrastructure.integration.LocalReturnInventoryGatewayAdapter;
import com.cashly.cashly_api.warehouses.stock.application.returns.services.ReturnInventoryService;

import com.cashly.cashly_api.credit_notes.domain.repository.CreditNoteRepository;
import com.cashly.cashly_api.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase;
import com.cashly.cashly_api.customers.balance.domain.valueobjects.BalanceType;
import com.cashly.cashly_api.debit_notes.domain.repository.DebitNoteRepository;
import com.cashly.cashly_api.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.returns.domain.entity.CancelledAllocation;
import com.cashly.cashly_api.returns.domain.entity.Return;
import com.cashly.cashly_api.returns.domain.valueobjects.ReturnId;
import com.cashly.cashly_api.returns.domain.entity.ReturnLine;
import com.cashly.cashly_api.returns.domain.entity.ReturnLineAllocation;
import com.cashly.cashly_api.returns.domain.valueobjects.ReturnNumber;
import com.cashly.cashly_api.returns.domain.valueobjects.ReturnType;
import com.cashly.cashly_api.returns.application.ports.out.ReturnInvoiceAdjustmentGateway;
import com.cashly.cashly_api.returns.domain.repository.ReturnRepository;
import com.cashly.cashly_api.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.valueobjects.ReferenceType;
import com.cashly.cashly_api.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.repository.StockMovementRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteReturnUseCase Unit Tests")
class DeleteReturnUseCaseTest {

    @Mock private ReturnInvoiceAdjustmentGateway invoiceAdjustmentGateway;
    @Mock private ReturnRepository returnRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private WarehouseStockRepository warehouseStockRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentAllocationRepository allocationRepository;
    @Mock private CreditNoteRepository creditNoteRepository;
    @Mock private DebitNoteRepository debitNoteRepository;

    private DeleteReturnUseCase useCase;

    @Captor private ArgumentCaptor<StockMovement> stockMovementCaptor;
    @Captor private ArgumentCaptor<PaymentAllocation> paymentAllocationCaptor;

    private static final UUID RETURN_ID = UUID.randomUUID();
    private static final UUID INVOICE_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final UUID WAREHOUSE_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID INVOICE_LINE_ID = UUID.randomUUID();
    private static final String USER_NAME = "Test User";

    private final ReturnId returnIdObj = ReturnId.of(RETURN_ID);
    private final InvoiceId invoiceIdObj = InvoiceId.of(INVOICE_ID);
    private static final String RETURN_NUMBER_STR = "RET-2026-0001";
    private static final String INVOICE_NUMBER_STR = "INV-2026-00001";

    @BeforeEach
    void setUp() {
        useCase = new DeleteReturnUseCase(invoiceAdjustmentGateway, returnRepository,
                new com.cashly.cashly_api.returns.infrastructure.integration.LocalReturnInvoiceQueryGateway(
                        new com.cashly.cashly_api.invoices.application.queries.services.RepositoryReturnInvoiceQuery(invoiceRepository),
                        new com.cashly.cashly_api.credit_notes.application.queries.services.RepositoryCreditNoteLedgerQuery(creditNoteRepository),
                        new com.cashly.cashly_api.debit_notes.application.queries.services.RepositoryDebitNoteLedgerQuery(debitNoteRepository)),
                new LocalReturnInventoryGatewayAdapter(new ReturnInventoryService(warehouseStockRepository, stockMovementRepository)),
                new com.cashly.cashly_api.returns.infrastructure.integration.LocalReturnPaymentSettlementGateway(
                        new com.cashly.cashly_api.payments.collection.application.services.ReturnPaymentOperationsService(paymentRepository, allocationRepository,
                                new com.cashly.cashly_api.payments.collection.domain.services.PaymentAllocationService(paymentRepository, allocationRepository))),
                new com.cashly.cashly_api.returns.infrastructure.integration.LocalReturnBalanceGateway(
                        new com.cashly.cashly_api.customers.balance.application.services.ReturnBalanceMutationService(
                                mock(com.cashly.cashly_api.customers.balance.application.add.use_case.AddToCustomerBalanceUseCase.class), consumeCustomerBalanceUseCase)));
    }

    // ------------------------------------------------------------------------
    // Test 1: Manual return with surplus — full revert flow
    // ------------------------------------------------------------------------
    @Test
    void should_RevertStockInvoiceAndBalance_When_DeletingManualReturn() {
        // Given
        Return return_ = buildManualReturn();
        WarehouseStock stock = givenStockExists(10);
        Invoice invoice = mockInvoice(InvoiceStatus.PAID, BigDecimal.valueOf(100));

        stubReturnFindById(return_);
        stubStockFind(WAREHOUSE_ID, PRODUCT_ID, stock);
        stubStockMovementSave();
        stubInvoiceFindById(invoice);
        stubAllocatedAmount(BigDecimal.valueOf(150));
        stubCreditNotes(List.of());
        stubDebitNotes(List.of());
        stubCalculateTotal(invoice, BigDecimal.valueOf(100));

        // When
        useCase.execute(RETURN_ID, CREATED_BY, USER_NAME);

        // Then — stock reverted
        verify(warehouseStockRepository).save(stock);
        assertEquals(9, stock.getQuantity(), "Stock should decrease by 1");

        // Then — CANCEL_ENTRY movement saved
        verify(stockMovementRepository).save(stockMovementCaptor.capture());
        StockMovement movement = stockMovementCaptor.getValue();
        assertEquals(MovementType.CANCEL_ENTRY, movement.getMovementType());
        assertEquals(ReferenceType.RETURN, movement.getReferenceType());
        assertEquals(RETURN_ID, movement.getReferenceId());
        assertEquals(RETURN_NUMBER_STR, movement.getReferenceNumber());
        assertTrue(movement.getNotes().contains("Return deleted:"));

        // Then — invoice reverted
        verify(invoiceAdjustmentGateway).reverse(eq(return_), any(), any(), any());
        verify(invoiceAdjustmentGateway).reverse(eq(return_),
                eq(BigDecimal.valueOf(150)),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO)
        );
        verify(invoiceAdjustmentGateway).reverse(eq(return_), any(), any(), any());

        // Then — customer balance consumed with surplus = 150 - 100 = 50
        verify(consumeCustomerBalanceUseCase).execute(
                eq(CUSTOMER_ID),
                eq(BalanceType.RETURNS),
                argThat(v -> ((BigDecimal) v).compareTo(BigDecimal.valueOf(50)) == 0),
                eq(BalanceType.RETURNS),
                eq(RETURN_ID),
                eq("REV-" + RETURN_NUMBER_STR),
                eq(INVOICE_NUMBER_STR),
                eq(CREATED_BY)
        );

        // Then — return deleted
        verify(returnRepository).delete(returnIdObj);
    }

    // ------------------------------------------------------------------------
    // Test 2: Automatic return with cancelled payments
    // ------------------------------------------------------------------------
    @Test
    void should_RevertPayments_When_DeletingAutomaticReturn() {
        // Given
        UUID paymentId = UUID.randomUUID();
        BigDecimal cancelledAmount = BigDecimal.valueOf(50);

        Return return_ = buildAutomaticReturn(List.of(
                CancelledAllocation.create(paymentId, INVOICE_ID, cancelledAmount)
        ));
        WarehouseStock stock = givenStockExists(5);
        Invoice invoice = mockInvoice(InvoiceStatus.PAID, BigDecimal.valueOf(100));
        Payment payment = mockPayment();

        stubReturnFindById(return_);
        stubStockFind(WAREHOUSE_ID, PRODUCT_ID, stock);
        stubStockMovementSave();
        stubInvoiceFindById(invoice);
        stubAllocatedAmount(BigDecimal.valueOf(200));
        stubCreditNotes(List.of());
        stubDebitNotes(List.of());
        stubCalculateTotal(invoice, BigDecimal.valueOf(100));
        when(paymentRepository.findById(PaymentId.of(paymentId))).thenReturn(Optional.of(payment));
        when(allocationRepository.save(any(PaymentAllocation.class))).thenAnswer(a -> a.getArgument(0));

        // When
        useCase.execute(RETURN_ID, CREATED_BY, USER_NAME);

        // Then — stock reverted (same as manual)
        verify(warehouseStockRepository).save(stock);
        assertEquals(4, stock.getQuantity());

        // Then — CANCEL_ENTRY movement saved
        verify(stockMovementRepository).save(stockMovementCaptor.capture());
        assertEquals(MovementType.CANCEL_ENTRY, stockMovementCaptor.getValue().getMovementType());

        // Then — invoice reverted
        verify(invoiceAdjustmentGateway).reverse(eq(return_), any(), any(), any());
        verify(invoiceAdjustmentGateway).reverse(eq(return_), any(), any(), any());

        // Then — payment reactivated
        verify(payment).reactivate();
        verify(paymentRepository).save(payment);

        // Then — PaymentAllocation created and saved for each cancelled allocation
        verify(allocationRepository, atLeastOnce()).save(paymentAllocationCaptor.capture());
        PaymentAllocation pa = paymentAllocationCaptor.getAllValues().stream()
                .filter(a -> a.getPaymentId().equals(paymentId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("PaymentAllocation for payment " + paymentId + " not found"));
        assertEquals(INVOICE_ID, pa.getInvoiceId());
        assertEquals(0, cancelledAmount.compareTo(pa.getAllocatedAmount()),
                "Allocated amount should match cancelled amount");

        // Then — customer balance consumed with surplus = 200 - 100 = 100
        verify(consumeCustomerBalanceUseCase).execute(
                eq(CUSTOMER_ID),
                eq(BalanceType.RETURNS),
                argThat(v -> ((BigDecimal) v).compareTo(BigDecimal.valueOf(100)) == 0),
                eq(BalanceType.RETURNS),
                eq(RETURN_ID),
                eq("REV-" + RETURN_NUMBER_STR),
                eq(INVOICE_NUMBER_STR),
                eq(CREATED_BY)
        );

        // Then — return deleted
        verify(returnRepository).delete(returnIdObj);
    }

    // ------------------------------------------------------------------------
    // Test 3: Return not found
    // ------------------------------------------------------------------------
    @Test
    void should_ThrowException_When_ReturnNotFound() {
        // Given
        when(returnRepository.findById(returnIdObj)).thenReturn(Optional.empty());

        // When
        GetReturnByIdUseCase.ReturnNotFoundException exception = assertThrows(
                GetReturnByIdUseCase.ReturnNotFoundException.class,
                () -> useCase.execute(RETURN_ID, CREATED_BY, USER_NAME)
        );

        // Then
        assertEquals("Return not found with id: " + RETURN_ID, exception.getMessage());
        verify(returnRepository).findById(returnIdObj);

        // No other repository should be called
        verifyNoInteractions(
                invoiceRepository,
                warehouseStockRepository,
                stockMovementRepository,
                consumeCustomerBalanceUseCase,
                paymentRepository,
                allocationRepository,
                creditNoteRepository,
                debitNoteRepository
        );
        verify(returnRepository, never()).delete(any());
    }

    // ------------------------------------------------------------------------
    // Test 4: Invoice is CANCELLED — skip revertReturn / save
    // ------------------------------------------------------------------------
    @Test
    void should_SkipInvoiceRevert_When_InvoiceIsCancelled() {
        // Given
        Return return_ = buildManualReturn();
        WarehouseStock stock = givenStockExists(5);
        Invoice invoice = mockInvoice(InvoiceStatus.CANCELLED, BigDecimal.valueOf(100));

        stubReturnFindById(return_);
        stubStockFind(WAREHOUSE_ID, PRODUCT_ID, stock);
        stubStockMovementSave();
        stubInvoiceFindById(invoice);
        stubAllocatedAmount(BigDecimal.ZERO);
        stubCreditNotes(List.of());
        stubDebitNotes(List.of());
        stubCalculateTotal(invoice, BigDecimal.valueOf(100));

        // When
        useCase.execute(RETURN_ID, CREATED_BY, USER_NAME);

        // Then — stock still reverted
        verify(warehouseStockRepository).save(stock);
        assertEquals(4, stock.getQuantity());

        // Then — CANCEL_ENTRY still saved
        verify(stockMovementRepository).save(any(StockMovement.class));

        // Then — invoice revert SKIPPED
        verifyNoInteractions(invoiceAdjustmentGateway);
        verify(invoiceRepository, never()).save(any());

        // Then — return deleted
        verify(returnRepository).delete(returnIdObj);
    }

    // ------------------------------------------------------------------------
    // Test 5: No surplus — skip customer balance consumption
    // ------------------------------------------------------------------------
    @Test
    void should_SkipBalanceRevert_When_NoSurplus() {
        // Given
        Return return_ = buildManualReturn();
        WarehouseStock stock = givenStockExists(8);
        Invoice invoice = mockInvoice(InvoiceStatus.PAID, BigDecimal.valueOf(200));

        stubReturnFindById(return_);
        stubStockFind(WAREHOUSE_ID, PRODUCT_ID, stock);
        stubStockMovementSave();
        stubInvoiceFindById(invoice);
        // totalPaid (50) < effectiveTotal (200) => surplus negative => no consumption
        stubAllocatedAmount(BigDecimal.valueOf(50));
        stubCreditNotes(List.of());
        stubDebitNotes(List.of());
        stubCalculateTotal(invoice, BigDecimal.valueOf(200));

        // When
        useCase.execute(RETURN_ID, CREATED_BY, USER_NAME);

        // Then — stock reverted
        verify(warehouseStockRepository).save(stock);
        assertEquals(7, stock.getQuantity());

        // Then — invoice reverted normally
        verify(invoiceAdjustmentGateway).reverse(eq(return_), any(), any(), any());
        verify(invoiceAdjustmentGateway).reverse(eq(return_), any(), any(), any());

        // Then — customer balance NOT consumed
        verify(consumeCustomerBalanceUseCase, never()).execute(
                any(), any(), any(), any(), any(), any(), any(), any()
        );

        // Then — return deleted
        verify(returnRepository).delete(returnIdObj);
    }

    // ========================================================================
    // Helpers — Return builders
    // ========================================================================

    private Return buildManualReturn() {
        return buildReturn(ReturnType.MANUAL, List.of());
    }

    private Return buildAutomaticReturn(List<CancelledAllocation> cancelledAllocations) {
        return buildReturn(ReturnType.AUTOMATIC, cancelledAllocations);
    }

    private Return buildReturn(ReturnType type, List<CancelledAllocation> cancelledAllocations) {
        ReturnLineAllocation allocation = ReturnLineAllocation.reconstitute(
                UUID.randomUUID(), WAREHOUSE_ID, BigDecimal.ONE
        );
        ReturnLine line = ReturnLine.create(
                INVOICE_LINE_ID,
                PRODUCT_ID,
                BigDecimal.ONE,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(50),
                List.of(allocation)
        );
        return Return.reconstitute(
                returnIdObj,
                ReturnNumber.of(2026, 1),
                invoiceIdObj.value(),
                CUSTOMER_ID,
                type,
                "Test reason",
                BigDecimal.valueOf(50),
                List.of(line),
                cancelledAllocations,
                CREATED_BY,
                "Test User",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // ========================================================================
    // Helpers — Stubs
    // ========================================================================

    private void stubReturnFindById(Return return_) {
        when(returnRepository.findById(returnIdObj)).thenReturn(Optional.of(return_));
    }

    private WarehouseStock givenStockExists(int initialQuantity) {
        WarehouseStock stock = new WarehouseStock(
                UUID.randomUUID(), WAREHOUSE_ID, PRODUCT_ID,
                initialQuantity, 0,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenReturn(stock);
        return stock;
    }

    private void stubStockFind(UUID warehouseId, UUID productId, WarehouseStock stock) {
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));
    }

    private void stubStockMovementSave() {
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(a -> a.getArgument(0));
    }

    private Invoice mockInvoice(InvoiceStatus status, BigDecimal effectiveTotal) {
        Invoice invoice = mock(Invoice.class);
        when(invoice.getStatus()).thenReturn(status);
        lenient().when(invoice.getInvoiceNumber()).thenReturn(InvoiceNumber.of(2026, 1));
        // calculateTotal will be stubbed individually per test
        return invoice;
    }

    private void stubCalculateTotal(Invoice invoice, BigDecimal effectiveTotal) {
        when(invoice.getSubtotal()).thenReturn(effectiveTotal);
        when(invoice.calculateTotal(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(effectiveTotal);
    }

    private void stubInvoiceFindById(Invoice invoice) {
        when(invoiceRepository.findById(invoiceIdObj)).thenReturn(Optional.of(invoice));
    }

    private void stubAllocatedAmount(BigDecimal amount) {
        when(allocationRepository.getAllocatedAmountByInvoiceId(INVOICE_ID)).thenReturn(amount);
    }

    private void stubCreditNotes(List<?> notes) {
        when(creditNoteRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of());
    }

    private void stubDebitNotes(List<?> notes) {
        when(debitNoteRepository.findByInvoiceId(INVOICE_ID)).thenReturn(List.of());
    }

    private Payment mockPayment() {
        Payment payment = mock(Payment.class);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        return payment;
    }
}
