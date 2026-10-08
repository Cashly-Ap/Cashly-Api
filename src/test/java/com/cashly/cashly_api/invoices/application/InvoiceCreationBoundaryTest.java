package com.cashly.cashly_api.invoices.application;
import com.cashly.cashly_api.invoices.application.usecases.CreateInvoiceUseCase;
import com.cashly.cashly_api.invoices.application.usecases.DeleteInvoiceUseCase;
import com.cashly.cashly_api.invoices.application.usecases.CancelInvoiceUseCase;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertTrue;
class InvoiceCreationBoundaryTest {
    @Test
    void lifecycle_dependencies_belong_to_invoices() {
        for (Class<?> useCase : new Class<?>[]{CreateInvoiceUseCase.class, DeleteInvoiceUseCase.class, CancelInvoiceUseCase.class}) {
            for (var constructor : useCase.getConstructors()) {
                assertTrue(Arrays.stream(constructor.getParameterTypes()).allMatch(type ->
                    type.getPackageName().startsWith("com.cashly.cashly_api.invoices.")), useCase.getSimpleName());
            }
        }
    }
}
