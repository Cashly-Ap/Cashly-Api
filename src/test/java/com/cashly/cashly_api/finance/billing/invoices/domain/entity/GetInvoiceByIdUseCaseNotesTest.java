package com.cashly.cashly_api.finance.billing.invoices.domain.entity;

import com.cashly.cashly_api.finance.billing.invoices.application.get.use_case.GetInvoiceByIdUseCase;

import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceNoteDetailsGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceReturnDetailsGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.StockAvailabilityGateway;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetInvoiceByIdUseCase Notes Tests")
class GetInvoiceByIdUseCaseNotesTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private StockAvailabilityGateway stockEnrichmentService;

    @Mock
    private InvoiceNoteDetailsGateway noteDetails;

    @Mock
    private InvoiceReturnDetailsGateway returnDetails;

    @InjectMocks
    private GetInvoiceByIdUseCase getInvoiceByIdUseCase;

    @Test
    void should_ReturnNotesAndComputedTotal_When_InvoiceHasNotes() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(stockEnrichmentService.enrich(any())).thenReturn(List.of());
        when(stockEnrichmentService.getAllStockByProducts(any(), any())).thenReturn(java.util.Map.of());
        UUID creditNoteId = UUID.randomUUID();
        UUID debitNoteId = UUID.randomUUID();
        when(noteDetails.findCreditNotes(id)).thenReturn(List.of(new InvoiceNoteDetailsGateway.Note(creditNoteId,
                "NC-2026-00001", BigDecimal.valueOf(200), "Damaged goods", LocalDateTime.now())));
        when(noteDetails.findDebitNotes(id)).thenReturn(List.of(new InvoiceNoteDetailsGateway.Note(debitNoteId,
                "ND-2026-00001", BigDecimal.valueOf(50), "Extra shipping", LocalDateTime.now())));
        when(returnDetails.findByInvoiceId(id)).thenReturn(List.of());

        InvoiceDetailResponse response = getInvoiceByIdUseCase.execute(id);

        assertNotNull(response);
        assertThat(response.creditApplied()).isEqualByComparingTo("200");
        assertThat(response.debitApplied()).isEqualByComparingTo("50");
        assertThat(response.total()).isEqualByComparingTo("850");
        assertThat(response.notesList()).hasSize(2);
        assertThat(response.notesList())
                .extracting(InvoiceDetailResponse.NoteInfo::noteNumber)
                .containsExactly("NC-2026-00001", "ND-2026-00001");
        verify(noteDetails).findCreditNotes(id);
        verify(noteDetails).findDebitNotes(id);
    }

    @Test
    void should_ReturnZeroNotes_When_InvoiceHasNoNotes() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(stockEnrichmentService.enrich(any())).thenReturn(List.of());
        when(stockEnrichmentService.getAllStockByProducts(any(), any())).thenReturn(java.util.Map.of());
        when(noteDetails.findCreditNotes(id)).thenReturn(List.of());
        when(noteDetails.findDebitNotes(id)).thenReturn(List.of());
        when(returnDetails.findByInvoiceId(id)).thenReturn(List.of());

        InvoiceDetailResponse response = getInvoiceByIdUseCase.execute(id);

        assertNotNull(response);
        assertThat(response.creditApplied()).isEqualByComparingTo("0");
        assertThat(response.debitApplied()).isEqualByComparingTo("0");
        assertThat(response.total()).isEqualByComparingTo("1000");
        assertThat(response.notesList()).isEmpty();
    }

    private Invoice buildInvoice(UUID id) {
        InvoiceLine line = InvoiceLine.create(
                UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.valueOf(1000), List.of()
        );
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
                InvoiceStatus.DRAFT,
                PaymentType.CASH,
                null,
                BigDecimal.ZERO,
                null,
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
