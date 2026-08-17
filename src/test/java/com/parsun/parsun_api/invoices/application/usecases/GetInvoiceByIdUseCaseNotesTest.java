package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.parsun.parsun_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.parsun.parsun_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.shared.application.StockEnrichmentService;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
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
    private StockEnrichmentService stockEnrichmentService;

    @Mock
    private CreditNoteRepository creditNoteRepository;

    @Mock
    private DebitNoteRepository debitNoteRepository;

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private GetInvoiceByIdUseCase getInvoiceByIdUseCase;

    @Test
    void should_ReturnNotesAndComputedTotal_When_InvoiceHasNotes() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(stockEnrichmentService.enrich(any())).thenReturn(List.of());
        when(stockEnrichmentService.getAllStockByProducts(any(), any())).thenReturn(java.util.Map.of());
        when(returnRepository.findAllWithFilters(any(), any(), any(), any())).thenReturn(List.of());

        UUID creditNoteId = UUID.randomUUID();
        CreditNote creditNote = CreditNote.reconstitute(
                CreditNoteId.of(creditNoteId),
                new CreditNoteNumber("NC-2026-00001"),
                UUID.randomUUID(), id,
                BigDecimal.valueOf(200), "Damaged goods",
                UUID.randomUUID(), "User",
                LocalDateTime.now(), LocalDateTime.now()
        );

        UUID debitNoteId = UUID.randomUUID();
        DebitNote debitNote = DebitNote.reconstitute(
                DebitNoteId.of(debitNoteId),
                new DebitNoteNumber("ND-2026-00001"),
                UUID.randomUUID(), id,
                BigDecimal.valueOf(50), "Extra shipping",
                UUID.randomUUID(), "User",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(creditNoteRepository.findByInvoiceId(id)).thenReturn(List.of(creditNote));
        when(debitNoteRepository.findByInvoiceId(id)).thenReturn(List.of(debitNote));

        InvoiceDetailResponse response = getInvoiceByIdUseCase.execute(id);

        assertNotNull(response);
        assertThat(response.creditApplied()).isEqualByComparingTo("200");
        assertThat(response.debitApplied()).isEqualByComparingTo("50");
        assertThat(response.total()).isEqualByComparingTo("850"); 
        assertThat(response.notesList()).hasSize(2);
        assertThat(response.notesList())
                .extracting(InvoiceDetailResponse.NoteInfo::noteNumber)
                .containsExactly("NC-2026-00001", "ND-2026-00001");
        verify(creditNoteRepository).findByInvoiceId(id);
        verify(debitNoteRepository).findByInvoiceId(id);
    }

    @Test
    void should_ReturnZeroNotes_When_InvoiceHasNoNotes() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(stockEnrichmentService.enrich(any())).thenReturn(List.of());
        when(stockEnrichmentService.getAllStockByProducts(any(), any())).thenReturn(java.util.Map.of());
        when(returnRepository.findAllWithFilters(any(), any(), any(), any())).thenReturn(List.of());
        when(creditNoteRepository.findByInvoiceId(id)).thenReturn(List.of());
        when(debitNoteRepository.findByInvoiceId(id)).thenReturn(List.of());

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
