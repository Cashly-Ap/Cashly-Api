package com.cashly.cashly_api.credit_notes.application.usecases;

import com.cashly.cashly_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.cashly.cashly_api.credit_notes.domain.entities.CreditNote;
import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import com.cashly.cashly_api.invoices.domain.entities.Invoice;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceNumber;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCreditNotesUseCase Unit Tests")
class GetCreditNotesUseCaseTest {

    @Mock
    private CreditNoteRepository creditNoteRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    private GetCreditNotesUseCase getCreditNotesUseCase;

    @BeforeEach
    void setUp() {
        getCreditNotesUseCase = new GetCreditNotesUseCase(creditNoteRepository, invoiceRepository);
    }

    @Test
    @DisplayName("should return list of credit note responses with invoice details")
    void shouldReturnCreditNoteResponsesWithInvoiceDetails() {
        UUID invoiceId1 = UUID.randomUUID();
        UUID invoiceId2 = UUID.randomUUID();
        CreditNote creditNote1 = CreditNote.reconstitute(
                CreditNoteId.generate(),
                CreditNoteNumber.of(2026, 1),
                UUID.randomUUID(),
                invoiceId1,
                new BigDecimal("150.00"),
                "Product damaged",
                UUID.randomUUID(),
                "John Doe",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        CreditNote creditNote2 = CreditNote.reconstitute(
                CreditNoteId.generate(),
                CreditNoteNumber.of(2026, 2),
                UUID.randomUUID(),
                invoiceId2,
                new BigDecimal("200.00"),
                "Service issue",
                UUID.randomUUID(),
                "Jane Doe",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Invoice invoice1 = mock(Invoice.class);
        InvoiceNumber invoiceNumber1 = mock(InvoiceNumber.class);
        when(invoice1.getId()).thenReturn(InvoiceId.of(invoiceId1));
        when(invoice1.getCustomerName()).thenReturn("Acme Corp");
        when(invoice1.getCustomerRif()).thenReturn("J-12345678-9");
        when(invoice1.getInvoiceNumber()).thenReturn(invoiceNumber1);
        when(invoiceNumber1.value()).thenReturn("INV-2026-00001");

        Invoice invoice2 = mock(Invoice.class);
        InvoiceNumber invoiceNumber2 = mock(InvoiceNumber.class);
        when(invoice2.getId()).thenReturn(InvoiceId.of(invoiceId2));
        when(invoice2.getCustomerName()).thenReturn("Beta Inc");
        when(invoice2.getCustomerRif()).thenReturn("J-98765432-1");
        when(invoice2.getInvoiceNumber()).thenReturn(invoiceNumber2);
        when(invoiceNumber2.value()).thenReturn("INV-2026-00002");

        when(creditNoteRepository.findAllWithFilters(null, null, null))
                .thenReturn(List.of(creditNote1, creditNote2));
        when(invoiceRepository.findAllById(List.of(InvoiceId.of(invoiceId1), InvoiceId.of(invoiceId2))))
                .thenReturn(List.of(invoice1, invoice2));

        List<CreditNoteResponse> responses = getCreditNotesUseCase.execute(null, null, null);

        assertNotNull(responses);
        assertEquals(2, responses.size());

        CreditNoteResponse response1 = responses.get(0);
        assertEquals(invoiceId1, response1.invoiceId());
        assertEquals("INV-2026-00001", response1.invoiceNumber());
        assertEquals("Acme Corp", response1.customerName());
        assertEquals("J-12345678-9", response1.customerRif());
        assertEquals(new BigDecimal("150.00"), response1.amount());

        CreditNoteResponse response2 = responses.get(1);
        assertEquals(invoiceId2, response2.invoiceId());
        assertEquals("INV-2026-00002", response2.invoiceNumber());
        assertEquals("Beta Inc", response2.customerName());
        assertEquals("J-98765432-1", response2.customerRif());
        assertEquals(new BigDecimal("200.00"), response2.amount());

        verify(creditNoteRepository).findAllWithFilters(null, null, null);
        verify(invoiceRepository).findAllById(List.of(InvoiceId.of(invoiceId1), InvoiceId.of(invoiceId2)));
    }

    @Test
    @DisplayName("should return list with null invoice details when invoices are not found")
    void shouldReturnCreditNoteResponsesWithNullInvoiceDetailsWhenInvoicesNotFound() {
        UUID invoiceId = UUID.randomUUID();
        CreditNote creditNote = CreditNote.reconstitute(
                CreditNoteId.generate(),
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

        when(creditNoteRepository.findAllWithFilters(null, null, null))
                .thenReturn(List.of(creditNote));
        when(invoiceRepository.findAllById(List.of(InvoiceId.of(invoiceId))))
                .thenReturn(List.of());

        List<CreditNoteResponse> responses = getCreditNotesUseCase.execute(null, null, null);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertNull(responses.get(0).customerName());
        assertNull(responses.get(0).customerRif());
        assertNull(responses.get(0).invoiceNumber());
    }

    @Test
    @DisplayName("should return empty list when no credit notes found")
    void shouldReturnEmptyListWhenNoCreditNotesFound() {
        when(creditNoteRepository.findAllWithFilters(null, null, null))
                .thenReturn(List.of());
        when(invoiceRepository.findAllById(List.of())).thenReturn(List.of());

        List<CreditNoteResponse> responses = getCreditNotesUseCase.execute(null, null, null);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(creditNoteRepository).findAllWithFilters(null, null, null);
        verify(invoiceRepository).findAllById(List.of());
    }

    @Test
    @DisplayName("should pass filters to repository when provided")
    void shouldPassFiltersToRepositoryWhenProvided() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        UUID customerId = UUID.randomUUID();

        when(creditNoteRepository.findAllWithFilters(dateFrom, dateTo, customerId))
                .thenReturn(List.of());

        List<CreditNoteResponse> responses = getCreditNotesUseCase.execute(dateFrom, dateTo, customerId);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(creditNoteRepository).findAllWithFilters(dateFrom, dateTo, customerId);
    }
}
