package com.cashly.cashly_api.finance.billing.invoices.application.list.use_case;

import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceListResponse;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceNoteDetailsGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoicePaymentTotalsGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetInvoicesUseCase Unit Tests")
class GetInvoicesUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoicePaymentTotalsGateway paymentTotals;

    @Mock
    private InvoiceNoteDetailsGateway noteDetails;

    private GetInvoicesUseCase getInvoicesUseCase;

    @BeforeEach
    void setUp() {
        getInvoicesUseCase = new GetInvoicesUseCase(invoiceRepository, paymentTotals, noteDetails);
    }

    @Test
    void should_ReturnPaginatedList_When_InvoicesExist() {
        UUID customerId = UUID.randomUUID();
        List<Invoice> invoices = List.of(
                buildInvoice(UUID.randomUUID(), customerId, InvoiceStatus.DRAFT),
                buildInvoice(UUID.randomUUID(), customerId, InvoiceStatus.PENDING_PAYMENT)
        );

        when(invoiceRepository.findAllWithFilters(any(), any(), any(), org.mockito.ArgumentMatchers.<String>anyList(), org.mockito.ArgumentMatchers.nullable(List.class), org.mockito.ArgumentMatchers.isNull(String.class), any(), any(), any(), any()))
                .thenReturn(invoices);
        when(paymentTotals.sumByInvoiceIds(any())).thenReturn(Map.of());
        when(noteDetails.sumDebitNotes(any())).thenReturn(Map.of());
        when(noteDetails.sumCreditNotes(any())).thenReturn(Map.of());

        List<InvoiceListResponse> result = getInvoicesUseCase.execute(
                null, null, null, null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(invoiceRepository).findAllWithFilters(
                null, null, null, List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID"),
                (List<String>) null, (String) null, null, null, null, null);
    }

    @Test
    void should_EnrichListWithPaymentAndNoteTotalsThroughModuleGateways() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = buildInvoice(invoiceId, UUID.randomUUID(), InvoiceStatus.PENDING_PAYMENT);
        when(invoiceRepository.findAllWithFilters(any(), any(), any(), org.mockito.ArgumentMatchers.<String>anyList(),
                org.mockito.ArgumentMatchers.nullable(List.class), org.mockito.ArgumentMatchers.isNull(String.class),
                any(), any(), any(), any())).thenReturn(List.of(invoice));
        when(paymentTotals.sumByInvoiceIds(List.of(invoiceId))).thenReturn(Map.of(invoiceId, new BigDecimal("3.00")));
        when(noteDetails.sumDebitNotes(List.of(invoiceId))).thenReturn(Map.of(invoiceId, new BigDecimal("2.00")));
        when(noteDetails.sumCreditNotes(List.of(invoiceId))).thenReturn(Map.of(invoiceId, new BigDecimal("1.00")));

        InvoiceListResponse response = getInvoicesUseCase.execute(null, null, null, null, null,
                null, null, null, null).getFirst();

        assertEquals(new BigDecimal("3.00"), response.paidAmount());
        assertEquals(new BigDecimal("2.00"), response.debitApplied());
        assertEquals(new BigDecimal("1.00"), response.creditApplied());
    }

    @Test
    void should_UseDefaultStatuses_When_StatusFilterIsNull() {
        when(invoiceRepository.findAllWithFilters(any(), any(), any(), org.mockito.ArgumentMatchers.<String>anyList(), org.mockito.ArgumentMatchers.nullable(List.class), org.mockito.ArgumentMatchers.isNull(String.class), any(), any(), any(), any()))
                .thenReturn(List.of());

        getInvoicesUseCase.execute(null, null, null, null, null, null, null, null, null);

        verify(invoiceRepository).findAllWithFilters(
                null, null, null, List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID"),
                (List<String>) null, (String) null, null, null, null, null);
    }

    @Test
    void should_UseDefaultStatuses_When_StatusFilterIsEmpty() {
        when(invoiceRepository.findAllWithFilters(any(), any(), any(), org.mockito.ArgumentMatchers.<String>anyList(), org.mockito.ArgumentMatchers.nullable(List.class), org.mockito.ArgumentMatchers.isNull(String.class), any(), any(), any(), any()))
                .thenReturn(List.of());

        getInvoicesUseCase.execute(null, null, null, List.of(), null, null, null, null, null);

        verify(invoiceRepository).findAllWithFilters(
                null, null, null, List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID"),
                (List<String>) null, (String) null, null, null, null, null);
    }

    @Test
    void should_PassCustomStatuses_When_Provided() {
        List<String> customStatuses = List.of("PAID", "CANCELLED");

        when(invoiceRepository.findAllWithFilters(any(), any(), any(), org.mockito.ArgumentMatchers.<String>anyList(), org.mockito.ArgumentMatchers.nullable(List.class), org.mockito.ArgumentMatchers.isNull(String.class), any(), any(), any(), any()))
                .thenReturn(List.of());

        getInvoicesUseCase.execute(null, null, null, customStatuses, null, null, null, null, null);

        verify(invoiceRepository).findAllWithFilters(
                null, null, null, customStatuses, (List<String>) null, (String) null, null, null, null, null);
    }

    @Test
    void should_PassFiltersToRepository_When_FiltersProvided() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(invoiceRepository.findAllWithFilters(any(), any(), any(), org.mockito.ArgumentMatchers.<String>anyList(), org.mockito.ArgumentMatchers.nullable(List.class), org.mockito.ArgumentMatchers.isNull(String.class), any(), any(), any(), any()))
                .thenReturn(List.of());

        getInvoicesUseCase.execute(dateFrom, dateTo, customerId, null, null, productId, categoryId, "createdAt", "desc");

        verify(invoiceRepository).findAllWithFilters(
                dateFrom, dateTo, customerId, List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID"),
                (List<String>) null, (String) null, productId, categoryId, "createdAt", "desc");
    }

    @Test
    void should_ReturnEmptyList_When_NoInvoicesMatch() {
        when(invoiceRepository.findAllWithFilters(any(), any(), any(), org.mockito.ArgumentMatchers.<String>anyList(), org.mockito.ArgumentMatchers.nullable(List.class), org.mockito.ArgumentMatchers.isNull(String.class), any(), any(), any(), any()))
                .thenReturn(List.of());

        List<InvoiceListResponse> result = getInvoicesUseCase.execute(
                null, null, null, null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void should_FilterByCustomerId_When_CustomerIdProvided() {
        UUID customerId = UUID.randomUUID();
        Invoice invoice = buildInvoice(UUID.randomUUID(), customerId, InvoiceStatus.DRAFT);

        when(invoiceRepository.findAllWithFilters(any(), any(), any(), org.mockito.ArgumentMatchers.<String>anyList(), org.mockito.ArgumentMatchers.nullable(List.class), org.mockito.ArgumentMatchers.isNull(String.class), any(), any(), any(), any()))
                .thenReturn(List.of(invoice));
        when(paymentTotals.sumByInvoiceIds(any())).thenReturn(Map.of());
        when(noteDetails.sumDebitNotes(any())).thenReturn(Map.of());
        when(noteDetails.sumCreditNotes(any())).thenReturn(Map.of());

        List<InvoiceListResponse> result = getInvoicesUseCase.execute(
                null, null, customerId, null, null, null, null, null, null);

        assertEquals(1, result.size());
        verify(invoiceRepository).findAllWithFilters(
                null, null, customerId, List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID"),
                (List<String>) null, (String) null, null, null, null, null);
    }

    private Invoice buildInvoice(UUID id, UUID customerId, InvoiceStatus status) {
        InvoiceLine line = InvoiceLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                customerId, "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name", null, null,
                status, PaymentType.CASH, null,
                BigDecimal.ZERO, null, null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
    }
    @Test
    void shouldQueryOnlyCancellableInvoicesRegardlessOfAllStatusRequest() {
        getInvoicesUseCase.execute(null, null, null, List.of("ALL"), null, null, null,
                null, null, "createdAt", "desc", com.cashly.cashly_api.shared.domain.DocumentAction.CANCEL);
        verify(invoiceRepository).findAllWithFilters(null, null, null, List.of("PENDING_PAYMENT", "PARTIALLY_PAID"),
                (List<String>) null, null, null, null, "createdAt", "desc");
    }

    @Test
    void shouldIntersectRequestedAndEligibleStatusesBeforeQueryingInvoices() {
        getInvoicesUseCase.execute(null, null, null, List.of("DRAFT", "PAID", "PARTIALLY_PAID"), "customer", null, null,
                null, null, "createdAt", "desc", com.cashly.cashly_api.shared.domain.DocumentAction.CANCEL);
        verify(invoiceRepository).findAllWithFilters(null, null, null, List.of("PARTIALLY_PAID"), "customer",
                (List<String>) null, null, null, null, "createdAt", "desc");
    }

    @Test
    void shouldNotQueryWhenNoRequestedInvoiceCanBeEdited() {
        var result = getInvoicesUseCase.execute(null, null, null, List.of("PAID"), null, null, null,
                null, null, "createdAt", "desc", com.cashly.cashly_api.shared.domain.DocumentAction.EDIT);
        assertTrue(result.isEmpty());
        verifyNoInteractions(invoiceRepository, paymentTotals, noteDetails);
    }

    @Test
    void shouldQueryOnlyDraftInvoicesForConfirmation() {
        getInvoicesUseCase.execute(null, null, null, null, null, null, null,
                null, null, "createdAt", "desc", com.cashly.cashly_api.shared.domain.DocumentAction.CONFIRM);
        verify(invoiceRepository).findAllWithFilters(null, null, null, List.of("DRAFT"),
                (List<String>) null, null, null, null, "createdAt", "desc");
    }

    @Test
    void shouldAllowDuplicatingInvoicesFromEveryStatus() {
        getInvoicesUseCase.execute(null, null, null, null, null, null, null,
                null, null, "createdAt", "desc", com.cashly.cashly_api.shared.domain.DocumentAction.DUPLICATE);
        verify(invoiceRepository).findAllWithFilters(null, null, null,
                java.util.Arrays.stream(InvoiceStatus.values()).map(Enum::name).toList(),
                (List<String>) null, null, null, null, "createdAt", "desc");
    }

}
