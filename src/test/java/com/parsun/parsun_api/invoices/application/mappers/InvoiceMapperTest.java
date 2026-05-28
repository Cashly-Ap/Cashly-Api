package com.parsun.parsun_api.invoices.application.mappers;

import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper.WarehouseProductKey;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvoiceMapper Tests")
class InvoiceMapperTest {

    @Test
    void toDetailResponse_ShouldComputeTotal_WithAppliedNotes() {
        Invoice invoice = buildInvoice(BigDecimal.valueOf(1000));
        BigDecimal creditApplied = BigDecimal.valueOf(100);
        BigDecimal debitApplied = BigDecimal.valueOf(50);
        List<InvoiceDetailResponse.NoteInfo> notes = List.of(
                new InvoiceDetailResponse.NoteInfo(
                        UUID.randomUUID(), "CREDIT", BigDecimal.valueOf(100),
                        "Test credit", LocalDateTime.now()
                ),
                new InvoiceDetailResponse.NoteInfo(
                        UUID.randomUUID(), "DEBIT", BigDecimal.valueOf(50),
                        "Test debit", LocalDateTime.now()
                )
        );

        InvoiceDetailResponse response = InvoiceMapper.toDetailResponse(
                invoice, creditApplied, debitApplied, notes
        );

        assertThat(response.creditApplied()).isEqualByComparingTo("100");
        assertThat(response.debitApplied()).isEqualByComparingTo("50");
        assertThat(response.total()).isEqualByComparingTo("950"); // 1000 - 100 + 50
        assertThat(response.notesList()).hasSize(2);
        assertThat(response.subtotal()).isEqualByComparingTo("1000");
    }

    @Test
    void toDetailResponse_ShouldHandleNoNotes() {
        Invoice invoice = buildInvoice(BigDecimal.valueOf(500));

        InvoiceDetailResponse response = InvoiceMapper.toDetailResponse(
                invoice, BigDecimal.ZERO, BigDecimal.ZERO, List.of()
        );

        assertThat(response.creditApplied()).isEqualByComparingTo("0");
        assertThat(response.debitApplied()).isEqualByComparingTo("0");
        assertThat(response.total()).isEqualByComparingTo("500"); // 500 - 0 + 0
        assertThat(response.notesList()).isEmpty();
        assertThat(response.subtotal()).isEqualByComparingTo("500");
    }

    @Test
    void toDetailResponse_ShouldHandleOnlyCreditNotes() {
        Invoice invoice = buildInvoice(BigDecimal.valueOf(2000));

        InvoiceDetailResponse response = InvoiceMapper.toDetailResponse(
                invoice, BigDecimal.valueOf(300), BigDecimal.ZERO, List.of(
                        new InvoiceDetailResponse.NoteInfo(
                                UUID.randomUUID(), "CREDIT", BigDecimal.valueOf(300),
                                "Bulk discount", LocalDateTime.now()
                        )
                )
        );

        assertThat(response.total()).isEqualByComparingTo("1700"); // 2000 - 300 + 0
        assertThat(response.creditApplied()).isEqualByComparingTo("300");
        assertThat(response.debitApplied()).isEqualByComparingTo("0");
    }

    @Test
    void toDetailResponse_ShouldHandleOnlyDebitNotes() {
        Invoice invoice = buildInvoice(BigDecimal.valueOf(2000));

        InvoiceDetailResponse response = InvoiceMapper.toDetailResponse(
                invoice, BigDecimal.ZERO, BigDecimal.valueOf(200), List.of(
                        new InvoiceDetailResponse.NoteInfo(
                                UUID.randomUUID(), "DEBIT", BigDecimal.valueOf(200),
                                "Additional charge", LocalDateTime.now()
                        )
                )
        );

        assertThat(response.total()).isEqualByComparingTo("2200"); // 2000 - 0 + 200
        assertThat(response.creditApplied()).isEqualByComparingTo("0");
        assertThat(response.debitApplied()).isEqualByComparingTo("200");
    }

    private Invoice buildInvoice(BigDecimal lineTotal) {
        InvoiceLine line = InvoiceLine.create(
                UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, lineTotal, List.of()
        );
        return Invoice.reconstitute(
                InvoiceId.of(UUID.randomUUID()),
                InvoiceNumber.of(2026, 1),
                UUID.randomUUID(),
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "Test User",
                null,
                null,
                InvoiceStatus.DRAFT,
                PaymentType.CASH,
                null,
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
            );
    }
}
