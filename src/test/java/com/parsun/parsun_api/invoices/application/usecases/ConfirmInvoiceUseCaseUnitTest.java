package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmInvoiceUseCase Unit Tests")
class ConfirmInvoiceUseCaseUnitTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    private ConfirmInvoiceUseCase confirmInvoiceUseCase;

    @BeforeEach
    void setUp() {
        confirmInvoiceUseCase = new ConfirmInvoiceUseCase(invoiceRepository);
    }

    @Test
    void should_ConfirmCashInvoice_When_InvoiceExistsAndIsDraft() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT, PaymentType.CASH);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);

        InvoiceDetailResponse response = confirmInvoiceUseCase.execute(id);

        assertNotNull(response);
        assertEquals("PAID", response.status());
        verify(invoiceRepository).findById(InvoiceId.of(id));
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void should_ConfirmCreditInvoice_When_InvoiceExistsAndIsDraft() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT, PaymentType.CREDIT);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);

        InvoiceDetailResponse response = confirmInvoiceUseCase.execute(id);

        assertNotNull(response);
        assertEquals("PENDING_PAYMENT", response.status());
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void should_ThrowInvoiceNotFoundException_When_InvoiceDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.empty());

        InvoiceNotFoundException exception = assertThrows(
                InvoiceNotFoundException.class,
                () -> confirmInvoiceUseCase.execute(id)
        );

        assertEquals("Invoice not found with id: " + id, exception.getMessage());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_ThrowIllegalStateException_When_InvoiceIsNotDraft() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.PAID, PaymentType.CASH);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));

        assertThrows(
                IllegalStateException.class,
                () -> confirmInvoiceUseCase.execute(id)
        );

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void should_SaveExactlyOnce_When_ConfirmationSucceeds() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT, PaymentType.CASH);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);

        confirmInvoiceUseCase.execute(id);

        verify(invoiceRepository, times(1)).save(invoice);
    }

    private Invoice buildInvoice(UUID id, InvoiceStatus status, PaymentType paymentType) {
        InvoiceLine line = InvoiceLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Invoice.reconstitute(
                InvoiceId.of(id),
                InvoiceNumber.of(2026, 1),
                UUID.randomUUID(),
                "Customer Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "User Name",
                null,
                null,
                status,
                paymentType,
                null,
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
