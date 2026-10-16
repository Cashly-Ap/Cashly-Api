package com.cashly.cashly_api.invoices.application.architecture;

import com.cashly.cashly_api.invoices.application.cancel.CancelInvoiceUseCase;
import com.cashly.cashly_api.invoices.application.confirm.ConfirmInvoiceUseCase;
import com.cashly.cashly_api.invoices.application.create.CreateInvoiceUseCase;
import com.cashly.cashly_api.invoices.application.delete.DeleteInvoiceUseCase;
import com.cashly.cashly_api.invoices.application.duplicate.DuplicateInvoiceUseCase;
import com.cashly.cashly_api.invoices.application.update.UpdateInvoiceUseCase;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InvoiceInventoryBoundaryTest {
    @Test void should_keep_inventory_implementation_types_out_of_invoice_usecases() {
        for (Class<?> useCase : List.of(CreateInvoiceUseCase.class, UpdateInvoiceUseCase.class,
                DuplicateInvoiceUseCase.class, DeleteInvoiceUseCase.class,
                ConfirmInvoiceUseCase.class, CancelInvoiceUseCase.class)) {
            for (var field : useCase.getDeclaredFields()) {
                assertFalse(field.getType().getPackageName().startsWith("com.cashly.cashly_api.warehouses"),
                        useCase.getSimpleName() + " depends on foreign inventory type " + field.getType().getName());
            }
            for (var constructor : useCase.getDeclaredConstructors()) {
                for (Class<?> parameter : constructor.getParameterTypes()) {
                    assertFalse(parameter.getPackageName().startsWith("com.cashly.cashly_api.warehouses"),
                            useCase.getSimpleName() + " exposes foreign inventory type " + parameter.getName());
                }
            }
        }
    }
}
