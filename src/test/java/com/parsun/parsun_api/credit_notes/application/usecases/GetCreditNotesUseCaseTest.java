package com.parsun.parsun_api.credit_notes.application.usecases;

import com.parsun.parsun_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import com.parsun.parsun_api.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.parsun.parsun_api.invoices.infrastructure.persistence.repositories.InvoiceJpaRepository;
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
    private InvoiceJpaRepository invoiceJpaRepository;

    private GetCreditNotesUseCase getCreditNotesUseCase;

    @BeforeEach
    void setUp() {
        getCreditNotesUseCase = new GetCreditNotesUseCase(creditNoteRepository, invoiceJpaRepository);
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

        InvoiceEntity invoice1 = new InvoiceEntity();
        invoice1.setId(invoiceId1);
        invoice1.setCustomerName("Acme Corp");
        invoice1.setCustomerRif("J-12345678-9");
        invoice1.setInvoiceNumber("INV-2026-00001");

        InvoiceEntity invoice2 = new InvoiceEntity();
        invoice2.setId(invoiceId2);
        invoice2.setCustomerName("Beta Inc");
        invoice2.setCustomerRif("J-98765432-1");
        invoice2.setInvoiceNumber("INV-2026-00002");

        when(creditNoteRepository.findAllWithFilters(null, null, null))
                .thenReturn(List.of(creditNote1, creditNote2));
        when(invoiceJpaRepository.findAllById(List.of(invoiceId1, invoiceId2)))
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
        verify(invoiceJpaRepository).findAllById(List.of(invoiceId1, invoiceId2));
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
        when(invoiceJpaRepository.findAllById(List.of(invoiceId)))
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
        when(invoiceJpaRepository.findAllById(List.of())).thenReturn(List.of());

        List<CreditNoteResponse> responses = getCreditNotesUseCase.execute(null, null, null);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(creditNoteRepository).findAllWithFilters(null, null, null);
        verify(invoiceJpaRepository).findAllById(List.of());
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
