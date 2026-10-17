package com.cashly.cashly_api.finance.treasury.receivables.reporting.application.architecture;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.generate_accounts_receivable_report_pdf.services.GenerateAccountsReceivableReportPdfUseCaseImpl;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertFalse;
class AccountsReceivableReportsBoundaryTest {
    @Test void reportDoesNotDependOnReceivablesInternalContracts() {
        Arrays.stream(GenerateAccountsReceivableReportPdfUseCaseImpl.class.getDeclaredFields()).forEach(field -> assertFalse(field.getType().getName().matches(".*cashly_api.finance.treasury.receivables.(?!reporting\\.).*"), field.toString()));
    }
}
