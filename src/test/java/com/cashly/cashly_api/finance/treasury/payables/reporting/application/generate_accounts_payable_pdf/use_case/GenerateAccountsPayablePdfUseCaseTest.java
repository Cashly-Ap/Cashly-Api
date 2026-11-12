package com.cashly.cashly_api.finance.treasury.payables.reporting.application.generate_accounts_payable_pdf.use_case;

import com.cashly.cashly_api.finance.treasury.payables.reporting.application.ports.out.AccountsPayableReportPdfPort;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateAccountsPayablePdfUseCaseTest {

    @Test
    void delegates_report_generation_to_the_pdf_port() {
        byte[] expected = new byte[]{1, 2, 3};
        UUID supplierId = UUID.randomUUID();
        AccountsPayableReportPdfPort port = (id, overdueOnly) -> {
            assertThat(id).isEqualTo(supplierId);
            assertThat(overdueOnly).isTrue();
            return expected;
        };

        byte[] result = new GenerateAccountsPayablePdfUseCase(port)
                .execute(supplierId, true);

        assertThat(result).isSameAs(expected);
    }
}
