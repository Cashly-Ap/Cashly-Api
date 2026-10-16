package com.cashly.cashly_api.invoices.application.architecture.operations;

import com.cashly.cashly_api.invoices.application.duplicate.DuplicateInvoiceUseCase;
import com.cashly.cashly_api.invoices.application.update.UpdateInvoiceUseCase;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceEditingBoundaryTest {
    @Test
    void update_and_duplicate_depend_only_on_invoice_contracts() {
        for (Class<?> useCase : new Class<?>[]{UpdateInvoiceUseCase.class, DuplicateInvoiceUseCase.class}) {
            assertTrue(Arrays.stream(useCase.getDeclaredFields()).allMatch(field ->
                    field.getType().getPackageName().startsWith("com.cashly.cashly_api.invoices.")),
                    useCase.getSimpleName() + " imports another module's repository");
        }
    }
}
