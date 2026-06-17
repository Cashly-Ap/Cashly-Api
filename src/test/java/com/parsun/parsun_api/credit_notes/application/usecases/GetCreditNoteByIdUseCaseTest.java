package com.parsun.parsun_api.credit_notes.application.usecases;

import com.parsun.parsun_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCreditNoteByIdUseCase Unit Tests")
class GetCreditNoteByIdUseCaseTest {

    @Mock
    private CreditNoteRepository creditNoteRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    private GetCreditNoteByIdUseCase getCreditNoteByIdUseCase;

    @BeforeEach
    void setUp() {
        getCreditNoteByIdUseCase = new GetCreditNoteByIdUseCase(creditNoteRepository, invoiceRepository);
    }

    @Test
    @DisplayName("should return credit note response when credit note exists and invoice is found")
    void shouldReturnCreditNoteResponseWhenCreditNoteExistsAndInvoiceFound() {
        UUID creditNoteId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        CreditNote creditNote = CreditNote.reconstitute(
                CreditNoteId.of(creditNoteId),
                CreditNoteNumber.of(2026, 1),
                UUID.randomUUID(),
                invoiceId,
                new BigDecimal("150.00"),
                "Product damaged",
                UUID.randomUUID(),
                "John Doe",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Invoice invoice = mock(Invoice.class);
        InvoiceNumber invoiceNumber = mock(InvoiceNumber.class);
        when(invoice.getCustomerName()).thenReturn("Acme Corp");
        when(invoice.getCustomerRif()).thenReturn("J-12345678-9");
        when(invoice.getInvoiceNumber()).thenReturn(invoiceNumber);
        when(invoiceNumber.value()).thenReturn("INV-2026-00001");

        when(creditNoteRepository.findById(CreditNoteId.of(creditNoteId))).thenReturn(Optional.of(creditNote));
        when(invoiceRepository.findById(InvoiceId.of(invoiceId))).thenReturn(Optional.of(invoice));

        CreditNoteResponse response = getCreditNoteByIdUseCase.execute(creditNoteId);

        assertNotNull(response);
        assertEquals(creditNoteId, response.id());
        assertEquals("CN-2026-0001", response.noteNumber());
        assertEquals(invoiceId, response.invoiceId());
        assertEquals("INV-2026-00001", response.invoiceNumber());
        assertEquals("Acme Corp", response.customerName());
        assertEquals("J-12345678-9", response.customerRif());
        assertEquals(new BigDecimal("150.00"), response.amount());
        assertEquals("Product damaged", response.reason());
        verify(creditNoteRepository).findById(CreditNoteId.of(creditNoteId));
        verify(invoiceRepository).findById(InvoiceId.of(invoiceId));
    }

    @Test
    @DisplayName("should return credit note response with null invoice details when invoice is not found")
    void shouldReturnCreditNoteResponseWithNullInvoiceDetailsWhenInvoiceNotFound() {
        UUID creditNoteId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        CreditNote creditNote = CreditNote.reconstitute(
                CreditNoteId.of(creditNoteId),
                CreditNoteNumber.of(2026, 1),
                UUID.randomUUID(),
                invoiceId,
                new BigDecimal("150.00"),
                "Product damaged",
                UUID.randomUUID(),
                "John Doe",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(creditNoteRepository.findById(CreditNoteId.of(creditNoteId))).thenReturn(Optional.of(creditNote));
        when(invoiceRepository.findById(InvoiceId.of(invoiceId))).thenReturn(Optional.empty());

        CreditNoteResponse response = getCreditNoteByIdUseCase.execute(creditNoteId);

        assertNotNull(response);
        assertEquals(creditNoteId, response.id());
        assertNull(response.customerName());
        assertNull(response.customerRif());
        assertNull(response.invoiceNumber());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when credit note does not exist")
    void shouldThrowWhenCreditNoteNotFound() {
        UUID creditNoteId = UUID.randomUUID();
        when(creditNoteRepository.findById(CreditNoteId.of(creditNoteId))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> getCreditNoteByIdUseCase.execute(creditNoteId));

        assertTrue(exception.getMessage().contains("Credit note not found"));
        assertTrue(exception.getMessage().contains(creditNoteId.toString()));
        verify(creditNoteRepository).findById(CreditNoteId.of(creditNoteId));
        verifyNoInteractions(invoiceRepository);
    }
}
