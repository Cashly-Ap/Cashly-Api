package com.cashly.cashly_api.invoices.application;

import com.cashly.cashly_api.invoices.application.usecases.UpdateInvoiceUseCase;
import com.cashly.cashly_api.invoices.application.usecases.DuplicateInvoiceUseCase;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceEditingBoundaryTest {
    @Test
    void editing_dependencies_belong_to_invoices() {
        for (Class<?> useCase : new Class<?>[]{UpdateInvoiceUseCase.class, DuplicateInvoiceUseCase.class}) {
            for (var constructor : useCase.getConstructors()) {
                assertTrue(Arrays.stream(constructor.getParameterTypes()).allMatch(type ->
                        type.getPackageName().startsWith("com.cashly.cashly_api.invoices.")),
                        useCase.getSimpleName() + " imports an external module into its constructor");
            }
        }
    }
}
