package com.parsun.parsun_api.returns.application.usecases;

import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceLineId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.services.PaymentAllocationService;
import com.parsun.parsun_api.returns.application.dto.request.CreateReturnRequest;
import com.parsun.parsun_api.returns.application.dto.response.ReturnResponse;
import com.parsun.parsun_api.returns.application.mappers.ReturnMapper;
import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
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
    private InvoiceRepository invoiceRepository;

    @Mock
    private ReturnMapper returnMapper;

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

    @Captor
    private ArgumentCaptor<BigDecimal> surplusCaptor;

    private CreateReturnUseCase useCase;

    private final UUID authenticatedUserId = UUID.randomUUID();
    private final String userName = "Test User";

    @BeforeEach
    void setUp() {
        useCase = new CreateReturnUseCase(
                returnRepository, invoiceRepository, returnMapper,
                paymentAllocationService, allocationRepository,
                addToCustomerBalanceUseCase, creditNoteRepository, debitNoteRepository,
                warehouseStockRepository, stockMovementRepository
        );
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
                        UUID.randomUUID(),
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
                InvoiceLineId.generate(), UUID.randomUUID(), "Product", "CODE",
                BigDecimal.ONE, subtotal, BigDecimal.ZERO, List.of()
        );
        return Invoice.reconstitute(
                InvoiceId.of(invoiceId), InvoiceNumber.of(2026, 1), UUID.randomUUID(),
                "Customer", "RIF",
                null, null, null, null,
                InvoiceStatus.PENDING_PAYMENT, null, null,
                returnedTotal, null,
                null, null,
                List.of(line), null, null
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
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(allocationRepository.findByInvoiceId(invoiceId)).thenReturn(List.of());
        when(allocationRepository.getAllocatedAmountByInvoiceId(invoiceId)).thenReturn(totalPaid);
        when(returnMapper.toResponse(any())).thenReturn(mock(ReturnResponse.class));

        useCase.execute(request, authenticatedUserId, userName);

        verify(allocationRepository).getAllocatedAmountByInvoiceId(invoiceId);
        verify(addToCustomerBalanceUseCase, never()).execute(
                any(), any(), any(), any(), any(), any(), any(), any()
        );
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
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
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
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
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
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(allocationRepository.getAllocatedAmountByInvoiceId(invoiceId)).thenReturn(totalPaid);
        when(returnMapper.toResponse(any())).thenReturn(mock(ReturnResponse.class));

        useCase.execute(request, authenticatedUserId, userName);

        verify(allocationRepository).getAllocatedAmountByInvoiceId(invoiceId);
        verify(paymentAllocationService, never()).cancelPayment(any());
        verify(addToCustomerBalanceUseCase, never()).execute(
                any(), any(), any(), any(), any(), any(), any(), any()
        );
    }
}
