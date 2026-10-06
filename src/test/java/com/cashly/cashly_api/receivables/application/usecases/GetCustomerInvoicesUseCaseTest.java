package com.cashly.cashly_api.receivables.application.usecases;

import com.cashly.cashly_api.receivables.application.dto.response.CustomerInvoiceDebtResponse;
import com.cashly.cashly_api.receivables.domain.repositories.ReceivablesRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCustomerInvoicesUseCase Unit Tests")
class GetCustomerInvoicesUseCaseTest {

    @Mock
    private ReceivablesRepository receivablesRepository;

    private GetCustomerInvoicesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCustomerInvoicesUseCase(receivablesRepository);
    }

    @Test
    void should_ReturnInvoices_When_CustomerHasInvoices() {
        UUID customerId = UUID.randomUUID();
        CustomerInvoiceDebtResponse invoice = new CustomerInvoiceDebtResponse(
                UUID.randomUUID(), "INV-2026-00001", "PENDING_PAYMENT",
                LocalDate.now(), new BigDecimal("1000.00"), BigDecimal.ZERO,
                new BigDecimal("500.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1000.00"), new BigDecimal("500.00"),
                null, null
        );
        Page<CustomerInvoiceDebtResponse> invoicePage = new PageImpl<>(
                List.of(invoice), PageRequest.of(0, 20), 1
        );

        when(receivablesRepository.findCustomerInvoices(customerId, null, null, 0, 20))
                .thenReturn(invoicePage);

        PageResponse<CustomerInvoiceDebtResponse> result = useCase.execute(customerId, null, null, 0, 20);

        assertNotNull(result);
        assertFalse(result.empty());
        assertEquals(1, result.content().size());
        assertEquals("INV-2026-00001", result.content().get(0).invoiceNumber());
        assertEquals(new BigDecimal("500.00"), result.content().get(0).balance());
        verify(receivablesRepository).findCustomerInvoices(customerId, null, null, 0, 20);
    }

    @Test
    void should_ReturnFilteredInvoices_When_StatusProvided() {
        UUID customerId = UUID.randomUUID();
        String status = "PENDING";

        Page<CustomerInvoiceDebtResponse> invoicePage = Page.empty();

        when(receivablesRepository.findCustomerInvoices(customerId, null, status, 0, 20))
                .thenReturn(invoicePage);

        PageResponse<CustomerInvoiceDebtResponse> result = useCase.execute(customerId, null, status, 0, 20);

        assertTrue(result.empty());
        verify(receivablesRepository).findCustomerInvoices(customerId, null, status, 0, 20);
    }

    @Test
    void should_ReturnFilteredInvoices_When_SearchProvided() {
        UUID customerId = UUID.randomUUID();
        String search = "INV-2026";

        Page<CustomerInvoiceDebtResponse> invoicePage = Page.empty();

        when(receivablesRepository.findCustomerInvoices(customerId, search, null, 0, 20))
                .thenReturn(invoicePage);

        PageResponse<CustomerInvoiceDebtResponse> result = useCase.execute(customerId, search, null, 0, 20);

        assertTrue(result.empty());
        verify(receivablesRepository).findCustomerInvoices(customerId, search, null, 0, 20);
    }

    @Test
    void should_ReturnCorrectPagination_When_MultipleInvoicesExist() {
        UUID customerId = UUID.randomUUID();
        CustomerInvoiceDebtResponse invoice = new CustomerInvoiceDebtResponse(
                UUID.randomUUID(), "INV-2026-00001", "PENDING_PAYMENT",
                LocalDate.now(), BigDecimal.TEN, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.TEN, BigDecimal.TEN, null, null
        );
        Page<CustomerInvoiceDebtResponse> invoicePage = new PageImpl<>(
                List.of(invoice), PageRequest.of(1, 5), 12
        );

        when(receivablesRepository.findCustomerInvoices(customerId, null, "PARTIAL", 1, 5))
                .thenReturn(invoicePage);

        PageResponse<CustomerInvoiceDebtResponse> result = useCase.execute(customerId, null, "PARTIAL", 1, 5);

        assertEquals(1, result.content().size());
        assertEquals(12, result.totalElements());
        assertEquals(3, result.totalPages());
        assertEquals(1, result.number());
        assertFalse(result.first());
    }
}
