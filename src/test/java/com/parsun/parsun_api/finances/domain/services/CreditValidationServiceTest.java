package com.parsun.parsun_api.finances.domain.services;

import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.finances.application.dto.response.CreditStatusResponse;
import com.parsun.parsun_api.finances.domain.repositories.*;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditValidationService Tests")
class CreditValidationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentAllocationRepository paymentAllocationRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private CreditNoteRepository creditNoteRepository;

    @Mock
    private DebitNoteRepository debitNoteRepository;

    @InjectMocks
    private CreditValidationService creditValidationService;

    @Test
    void getCreditStatus_ShouldIncludeNotesInPendingBalance() {
        UUID customerId = UUID.randomUUID();
        Customer customer = mock(Customer.class);
        when(customer.getCreditLimit()).thenReturn(new com.parsun.parsun_api.customers.domain.valueobjects.CreditLimit(BigDecimal.valueOf(5000)));
        when(customer.getCreditDays()).thenReturn(new com.parsun.parsun_api.customers.domain.valueobjects.CreditDays(30));
        when(customer.getLegalName()).thenReturn(new com.parsun.parsun_api.customers.domain.valueobjects.LegalName("Test Customer"));
        when(customer.getRif()).thenReturn(new com.parsun.parsun_api.customers.domain.valueobjects.Rif("J-12345678-9"));
        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));

        InvoiceLine line = InvoiceLine.create(
                UUID.randomUUID(), "Product", "PROD",
                BigDecimal.ONE, BigDecimal.valueOf(1000), List.of()
        );
        Invoice invoice = Invoice.reconstitute(
                InvoiceId.of(UUID.randomUUID()),
                InvoiceNumber.of(2026, 1),
                customerId, "Test", "J-12345678-9",
                UUID.randomUUID(), "User",
                null, null,
                InvoiceStatus.PENDING_PAYMENT, PaymentType.CREDIT, null,
                null, null,
                List.of(line), LocalDateTime.now(), LocalDateTime.now()
        );
        when(invoiceRepository.findAllWithFilters(any(), any(), eq(customerId), any(), any(), any(), any(), any()))
                .thenReturn(List.of(invoice));
        when(paymentAllocationRepository.findByInvoiceId(any())).thenReturn(List.of());
        lenient().when(creditNoteRepository.findByInvoiceId(any())).thenReturn(List.of());
        lenient().when(debitNoteRepository.findByInvoiceId(any())).thenReturn(List.of());

        CreditStatusResponse response = creditValidationService.getCreditStatus(customerId);

        assertThat(response).isNotNull();
        assertThat(response.pendingBalance()).isEqualByComparingTo("1000");
    }
}
