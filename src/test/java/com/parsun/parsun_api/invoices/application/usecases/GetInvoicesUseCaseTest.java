package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceListResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetInvoicesUseCase Unit Tests")
class GetInvoicesUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    private GetInvoicesUseCase getInvoicesUseCase;

    @BeforeEach
    void setUp() {
        getInvoicesUseCase = new GetInvoicesUseCase(invoiceRepository);
    }

    @Test
    void should_ReturnPaginatedList_When_InvoicesExist() {
        UUID customerId = UUID.randomUUID();
        List<Invoice> invoices = List.of(
                buildInvoice(UUID.randomUUID(), customerId, InvoiceStatus.DRAFT),
                buildInvoice(UUID.randomUUID(), customerId, InvoiceStatus.PENDING_PAYMENT)
        );

        when(invoiceRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(invoices);

        List<InvoiceListResponse> result = getInvoicesUseCase.execute(
                null, null, null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(invoiceRepository).findAllWithFilters(
                null, null, null, List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null);
    }

    @Test
    void should_UseDefaultStatuses_When_StatusFilterIsNull() {
        when(invoiceRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        getInvoicesUseCase.execute(null, null, null, null, null, null, null, null);

        verify(invoiceRepository).findAllWithFilters(
                null, null, null, List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null);
    }

    @Test
    void should_UseDefaultStatuses_When_StatusFilterIsEmpty() {
        when(invoiceRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        getInvoicesUseCase.execute(null, null, null, List.of(), null, null, null, null);

        verify(invoiceRepository).findAllWithFilters(
                null, null, null, List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null);
    }

    @Test
    void should_PassCustomStatuses_When_Provided() {
        List<String> customStatuses = List.of("PAID", "CANCELLED");

        when(invoiceRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        getInvoicesUseCase.execute(null, null, null, customStatuses, null, null, null, null);

        verify(invoiceRepository).findAllWithFilters(
                null, null, null, customStatuses, null, null, null, null);
    }

    @Test
    void should_PassFiltersToRepository_When_FiltersProvided() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(invoiceRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        getInvoicesUseCase.execute(dateFrom, dateTo, customerId, null, productId, categoryId, "createdAt", "desc");

        verify(invoiceRepository).findAllWithFilters(
                dateFrom, dateTo, customerId, List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID"),
                productId, categoryId, "createdAt", "desc");
    }

    @Test
    void should_ReturnEmptyList_When_NoInvoicesMatch() {
        when(invoiceRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        List<InvoiceListResponse> result = getInvoicesUseCase.execute(
                null, null, null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void should_FilterByCustomerId_When_CustomerIdProvided() {
        UUID customerId = UUID.randomUUID();
        Invoice invoice = buildInvoice(UUID.randomUUID(), customerId, InvoiceStatus.DRAFT);

        when(invoiceRepository.findAllWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(invoice));

        List<InvoiceListResponse> result = getInvoicesUseCase.execute(
                null, null, customerId, null, null, null, null, null);

        assertEquals(1, result.size());
        verify(invoiceRepository).findAllWithFilters(
                null, null, customerId, List.of("DRAFT", "PENDING_PAYMENT", "PARTIALLY_PAID"),
                null, null, null, null);
    }

    private Invoice buildInvoice(UUID id, UUID customerId, InvoiceStatus status) {
        InvoiceLine line = InvoiceLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Invoice.reconstitute(
                InvoiceId.of(id), InvoiceNumber.of(2026, 1),
                customerId, "Customer Name", "J-12345678-9",
                UUID.randomUUID(), "User Name", null, null,
                status, PaymentType.CASH, null,
                null, null, List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
