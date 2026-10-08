package com.cashly.cashly_api.reports.finance.application;
import com.cashly.cashly_api.reports.finance.application.usecases.GenerateAccountsReceivableReportPdfUseCaseImpl;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertFalse;
class AccountsReceivableReportsBoundaryTest {
    @Test void reportDoesNotDependOnReceivablesInternalContracts() {
        Arrays.stream(GenerateAccountsReceivableReportPdfUseCaseImpl.class.getDeclaredFields()).forEach(field -> assertFalse(field.getType().getName().contains("cashly_api.receivables."), field.toString()));
    }
}
