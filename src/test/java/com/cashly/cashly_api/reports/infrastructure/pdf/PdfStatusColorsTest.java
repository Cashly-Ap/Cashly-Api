package com.cashly.cashly_api.reports.infrastructure.pdf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PdfStatusColorsTest {

    @Test
    void shouldUseApplicationSemanticColorsForStatuses() {
        assertThat(PdfStatusColors.forStatus("DRAFT"))
                .isSameAs(PdfStatusColors.forStatus("PARTIALLY_PAID"));
        assertThat(PdfStatusColors.forStatus("CONFIRMED"))
                .isSameAs(PdfStatusColors.forStatus("CONVERTED_TO_ORDER"));
        assertThat(PdfStatusColors.forStatus("DRAFT"))
                .isSameAs(PdfStatusColors.forStatus("PARTIALLY_PAID"));
        assertThat(PdfStatusColors.forStatus("CANCELLED"))
                .isSameAs(PdfStatusColors.forStatus("OVERDUE"));
    }

    @Test
    void shouldUseInfoColorForUnknownStatuses() {
        assertThat(PdfStatusColors.forStatus("UNKNOWN"))
                .isSameAs(PdfStatusColors.forStatus("DRAFT"));
        assertThat(PdfStatusColors.forStatus(null))
                .isSameAs(PdfStatusColors.forStatus("DRAFT"));
    }
}
