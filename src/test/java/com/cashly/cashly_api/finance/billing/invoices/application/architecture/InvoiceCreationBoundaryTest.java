package com.cashly.cashly_api.finance.billing.invoices.application.architecture;
import com.cashly.cashly_api.finance.billing.invoices.application.create.use_case.CreateInvoiceUseCase;
import com.cashly.cashly_api.finance.billing.invoices.application.delete.use_case.DeleteInvoiceUseCase;
import com.cashly.cashly_api.finance.billing.invoices.application.cancel.use_case.CancelInvoiceUseCase;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertTrue;
class InvoiceCreationBoundaryTest {
    @Test
    void lifecycle_dependencies_belong_to_invoices() {
        for (Class<?> useCase : new Class<?>[]{CreateInvoiceUseCase.class,
                DeleteInvoiceUseCase.class, CancelInvoiceUseCase.class}) {
            for (var constructor : useCase.getConstructors()) {
                assertTrue(Arrays.stream(constructor.getParameterTypes())
                        .allMatch(type -> (type.getPackageName().startsWith(
                                "com.cashly.cashly_api.finance.billing.invoices.") || type.getPackageName().startsWith("com.cashly.cashly_api.platform.notifications.application.ports.in"))),
                        useCase.getSimpleName());
            }
        }
    }
}
