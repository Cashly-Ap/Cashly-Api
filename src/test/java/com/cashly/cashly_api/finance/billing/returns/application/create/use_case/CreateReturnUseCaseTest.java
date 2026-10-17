package com.cashly.cashly_api.finance.billing.returns.application.create.use_case;

import com.cashly.cashly_api.finance.billing.returns.infrastructure.integration.LocalReturnInventoryGatewayAdapter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.returns.services.ReturnInventoryService;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.billing.credit_notes.domain.repository.CreditNoteRepository;
import com.cashly.cashly_api.partners.customers.balance.application.add.use_case.AddToCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.domain.valueobjects.BalanceType;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.repository.DebitNoteRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceLineId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.services.PaymentAllocationService;
import com.cashly.cashly_api.finance.billing.returns.application.create.request.CreateReturnRequest;
import com.cashly.cashly_api.finance.billing.returns.application.dto.response.ReturnResponse;
import com.cashly.cashly_api.finance.billing.returns.application.mappers.ReturnMapper;
import com.cashly.cashly_api.finance.billing.returns.application.services.ReturnResponseAssembler;
import com.cashly.cashly_api.finance.billing.returns.domain.entity.Return;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInvoiceAdjustmentGateway;
import com.cashly.cashly_api.finance.billing.returns.infrastructure.integration.InvoiceReturnAdjustmentAdapter;
import com.cashly.cashly_api.finance.billing.invoices.application.queries.services.RepositoryInvoiceReturnAdjustments;
import com.cashly.cashly_api.finance.billing.returns.domain.repository.ReturnRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateReturnUseCase Unit Tests")
class CreateReturnUseCaseTest {

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private ReturnInvoiceAdjustmentGateway invoiceAdjustmentGateway;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ReturnResponseAssembler returnMapper;

    @Mock
    private PaymentAllocationService paymentAllocationService;

    @Mock
    private PaymentAllocationRepository allocationRepository;

    @Mock
    private AddToCustomerBalanceUseCase addToCustomerBalanceUseCase;

    @Mock
    private CreditNoteRepository creditNoteRepository;

    @Mock
    private DebitNoteRepository debitNoteRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private CashMovementService cashMovementService;

    @Captor
    private ArgumentCaptor<BigDecimal> surplusCaptor;

    private CreateReturnUseCase useCase;

    private final java.util.Map<UUID, UUID> invoiceLineIds = new java.util.HashMap<>();

    private final UUID authenticatedUserId = UUID.randomUUID();
    private final String userName = "Test User";

    @BeforeEach
    void setUp() {
        invoiceAdjustmentGateway = new InvoiceReturnAdjustmentAdapter(new RepositoryInvoiceReturnAdjustments(invoiceRepository));
        useCase = createUseCase(returnRepository);
        when(creditNoteRepository.findByInvoiceId(any())).thenReturn(List.of());
        when(debitNoteRepository.findByInvoiceId(any())).thenReturn(List.of());


        WarehouseStock mockStock = WarehouseStock.create(UUID.randomUUID(), UUID.randomUUID());
        mockStock.addStock(1000);
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(any(), any()))
                .thenReturn(Optional.of(mockStock));
        when(warehouseStockRepository.save(any(WarehouseStock.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private CreateReturnRequest buildRequest(BigDecimal total, String returnType, UUID invoiceId, UUID customerId) {
        return new CreateReturnRequest(
                invoiceId,
                customerId,
                returnType,
                "Test reason",
                List.of(new CreateReturnRequest.ReturnLineRequest(
                        invoiceLineIds.computeIfAbsent(invoiceId, ignored -> UUID.randomUUID()),
                        UUID.randomUUID(),
                        BigDecimal.ONE,
                        total,
                        total,
                        null,
                        List.of(new CreateReturnRequest.ReturnLineRequest.AllocationRequest(
                                UUID.randomUUID(),
                                BigDecimal.ONE
                        ))
                ))
        );
    }

    private Invoice buildInvoice(BigDecimal subtotal, BigDecimal returnedTotal, UUID invoiceId) {
        InvoiceLine line = InvoiceLine.reconstitute(
                InvoiceLineId.of(invoiceLineIds.computeIfAbsent(invoiceId, ignored -> UUID.randomUUID())), UUID.randomUUID(), "Product", "CODE",
                BigDecimal.ONE, subtotal, BigDecimal.ZERO, List.of()
        );
        return Invoice.reconstitute(
                InvoiceId.of(invoiceId), InvoiceNumber.of(2026, 1), UUID.randomUUID(),
                "Customer", "RIF",
                null, null, null, null,
                InvoiceStatus.PENDING_PAYMENT, null, null,
                returnedTotal, null,
                null, null,
                List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void should_NotCreateSurplus_When_AutomaticReturnAndNoTotalPaid() {
        UUID invoiceId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        BigDecimal subtotal = new BigDecimal("50.00");
        BigDecimal returnTotal = new BigDecimal("50.00");
        BigDecimal totalPaid = BigDecimal.ZERO;

        CreateReturnRequest request = buildRequest(returnTotal, "AUTOMATIC", invoiceId, customerId);
        Invoice invoice = buildInvoice(subtotal, BigDecimal.ZERO, invoiceId);

        when(returnRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(returnRepository.save(any(Return.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.of(invoice));
        when(allocationRepository.findByInvoiceId(invoiceId)).thenReturn(List.of());
        when(allocationRepository.getAllocatedAmountByInvoiceId(invoiceId)).thenReturn(totalPaid);
        when(returnMapper.toResponse(any())).thenReturn(mock(ReturnResponse.class));

        useCase.execute(request, authenticatedUserId, userName);

        verify(allocationRepository).getAllocatedAmountByInvoiceId(invoiceId);
        verify(addToCustomerBalanceUseCase, never()).execute(
                any(), any(), any(), any(), any(), any(), any(), any()
        );
        verify(cashMovementService).record(
                eq(CashMovementDirection.OUT), eq(CashMovementType.RETURN),
                eq(returnTotal), isNull(), contains("RET-"), eq("RETURN"),
                any(UUID.class), eq(authenticatedUserId), eq(userName));
    }

    @Test
    void should_CreateSurplus_When_AutomaticReturnAndTotalPaidExceedsRemaining() {
        UUID invoiceId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        BigDecimal subtotal = new BigDecimal("50.00");
        BigDecimal returnTotal = new BigDecimal("50.00");
        BigDecimal totalPaid = new BigDecimal("25.00");

        CreateReturnRequest request = buildRequest(returnTotal, "AUTOMATIC", invoiceId, customerId);
        Invoice invoice = buildInvoice(subtotal, BigDecimal.ZERO, invoiceId);

        when(returnRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(returnRepository.save(any(Return.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.of(invoice));
        when(allocationRepository.findByInvoiceId(invoiceId)).thenReturn(List.of());
        when(allocationRepository.getAllocatedAmountByInvoiceId(invoiceId)).thenReturn(totalPaid);
        when(returnMapper.toResponse(any())).thenReturn(mock(ReturnResponse.class));

        useCase.execute(request, authenticatedUserId, userName);

        verify(allocationRepository).getAllocatedAmountByInvoiceId(invoiceId);
        verify(addToCustomerBalanceUseCase).execute(
                eq(customerId),
                eq(BalanceType.RETURNS),
                surplusCaptor.capture(),
                eq(BalanceType.RETURNS),
                any(UUID.class),
                anyString(),
                anyString(),
                eq(authenticatedUserId)
        );
        assertEquals(0, new BigDecimal("25.00").compareTo(surplusCaptor.getValue()));
    }

    @Test
    void should_CreateSurplus_When_ManualReturnAndTotalPaidExceedsRemaining() {
        UUID invoiceId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        BigDecimal subtotal = new BigDecimal("50.00");
        BigDecimal returnTotal = new BigDecimal("40.00");
        BigDecimal totalPaid = new BigDecimal("25.00");

        CreateReturnRequest request = buildRequest(returnTotal, "MANUAL", invoiceId, customerId);
        Invoice invoice = buildInvoice(subtotal, BigDecimal.ZERO, invoiceId);

        when(returnRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(returnRepository.save(any(Return.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.of(invoice));
        when(allocationRepository.getAllocatedAmountByInvoiceId(invoiceId)).thenReturn(totalPaid);
        when(returnMapper.toResponse(any())).thenReturn(mock(ReturnResponse.class));

        useCase.execute(request, authenticatedUserId, userName);

        verify(allocationRepository).getAllocatedAmountByInvoiceId(invoiceId);
        verify(paymentAllocationService, never()).cancelPayment(any());
        verify(addToCustomerBalanceUseCase).execute(
                eq(customerId),
                eq(BalanceType.RETURNS),
                surplusCaptor.capture(),
                eq(BalanceType.RETURNS),
                any(UUID.class),
                anyString(),
                anyString(),
                eq(authenticatedUserId)
        );
        assertEquals(0, new BigDecimal("15.00").compareTo(surplusCaptor.getValue()));
    }

    @Test
    void should_NotCreateSurplus_When_ManualReturnAndTotalPaidBelowRemaining() {
        UUID invoiceId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        BigDecimal subtotal = new BigDecimal("50.00");
        BigDecimal returnTotal = new BigDecimal("20.00");
        BigDecimal totalPaid = new BigDecimal("25.00");

        CreateReturnRequest request = buildRequest(returnTotal, "MANUAL", invoiceId, customerId);
        Invoice invoice = buildInvoice(subtotal, BigDecimal.ZERO, invoiceId);

        when(returnRepository.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(returnRepository.save(any(Return.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.of(invoice));
        when(allocationRepository.getAllocatedAmountByInvoiceId(invoiceId)).thenReturn(totalPaid);
        when(returnMapper.toResponse(any())).thenReturn(mock(ReturnResponse.class));

        useCase.execute(request, authenticatedUserId, userName);

        verify(allocationRepository).getAllocatedAmountByInvoiceId(invoiceId);
        verify(paymentAllocationService, never()).cancelPayment(any());
        verify(addToCustomerBalanceUseCase, never()).execute(
                any(), any(), any(), any(), any(), any(), any(), any()
        );
    }
    @Test
    void should_persist_cancelled_payment_snapshot_for_subsequent_deletion() {
        UUID invoiceId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        var request = buildRequest(new BigDecimal("10.00"), "AUTOMATIC", invoiceId, customerId);
        var invoice = buildInvoice(new BigDecimal("50.00"), BigDecimal.ZERO, invoiceId);
        var entityManager = mock(jakarta.persistence.EntityManager.class);
        var stored = new java.util.concurrent.atomic.AtomicReference<com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnEntity>();
        when(entityManager.find(eq(com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnEntity.class), any()))
                .thenAnswer(invocation -> stored.get());
        doAnswer(invocation -> { stored.set(invocation.getArgument(0)); return null; }).when(entityManager)
                .persist(any(com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnEntity.class));
        when(entityManager.merge(any())).thenAnswer(invocation -> {
            Object entity = invocation.getArgument(0);
            if (entity instanceof com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnEntity returnEntity) stored.set(returnEntity);
            return entity;
        });
        when(entityManager.find(eq(com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.entities.ReturnNumberSequenceEntity.class),
                any(), eq(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE))).thenReturn(null);
        var repository = new com.cashly.cashly_api.finance.billing.returns.infrastructure.persistence.adapters.JpaReturnRepository(entityManager);
        var persistedUseCase = createUseCase(repository);
        when(invoiceRepository.findById(any(InvoiceId.class))).thenReturn(Optional.of(invoice));
        when(allocationRepository.getAllocatedAmountByInvoiceId(invoiceId)).thenReturn(new BigDecimal("15.00"));
        when(allocationRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(
                com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation.create(paymentId, invoiceId, new BigDecimal("15.00"))));
        when(returnMapper.toResponse(any())).thenReturn(mock(ReturnResponse.class));
        persistedUseCase.execute(request, authenticatedUserId, userName);
        var reloaded = repository.findById(com.cashly.cashly_api.finance.billing.returns.domain.valueobjects.ReturnId.of(stored.get().getId())).orElseThrow();
        assertEquals(1, reloaded.getCancelledAllocations().size(), "Cancelled allocations must survive repository reload for deletion");
        assertEquals(paymentId, reloaded.getCancelledAllocations().getFirst().getPaymentId());
        assertEquals(invoiceId, reloaded.getCancelledAllocations().getFirst().getInvoiceId());
        assertEquals(new BigDecimal("15.00"), reloaded.getCancelledAllocations().getFirst().getAmount());
    }
    private CreateReturnUseCase createUseCase(ReturnRepository repository) {
        var query = new com.cashly.cashly_api.finance.billing.returns.infrastructure.integration.LocalReturnInvoiceQueryGateway(
                new com.cashly.cashly_api.finance.billing.invoices.application.queries.services.RepositoryReturnInvoiceQuery(invoiceRepository),
                new com.cashly.cashly_api.finance.billing.credit_notes.application.queries.services.RepositoryCreditNoteLedgerQuery(creditNoteRepository),
                new com.cashly.cashly_api.finance.billing.debit_notes.application.queries.services.RepositoryDebitNoteLedgerQuery(debitNoteRepository));
        var payments = new com.cashly.cashly_api.finance.billing.returns.infrastructure.integration.LocalReturnPaymentSettlementGateway(
                new com.cashly.cashly_api.finance.treasury.payments.collection.application.services.ReturnPaymentOperationsService(
                        mock(com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository.class), allocationRepository, paymentAllocationService));
        var balances = new com.cashly.cashly_api.finance.billing.returns.infrastructure.integration.LocalReturnBalanceGateway(
                new com.cashly.cashly_api.partners.customers.balance.application.services.ReturnBalanceMutationService(addToCustomerBalanceUseCase,
                        mock(com.cashly.cashly_api.partners.customers.balance.application.consume.use_case.ConsumeCustomerBalanceUseCase.class)));
        var cash = new com.cashly.cashly_api.finance.billing.returns.infrastructure.integration.LocalReturnCashGateway(
                new com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.services.ReturnCashRecordingService(cashMovementService));
        return new CreateReturnUseCase(invoiceAdjustmentGateway, repository, returnMapper, query, payments, balances,
                new LocalReturnInventoryGatewayAdapter(new ReturnInventoryService(warehouseStockRepository, stockMovementRepository)), cash);
    }}
