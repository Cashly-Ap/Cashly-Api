package com.cashly.cashly_api.reports.invoices.application;

import com.cashly.cashly_api.reports.invoices.application.port.InvoicePdfPort;
import com.cashly.cashly_api.reports.invoices.application.port.InvoicesReportPdfPort;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicePdfUseCaseImpl;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InvoiceReportsBoundaryTest {
    @Test
    void applicationContractsDoNotExposeInvoiceModuleDtosOrConcreteUseCases() {
        for (Class<?> type : new Class<?>[]{InvoicePdfPort.class, InvoicesReportPdfPort.class, GenerateInvoicePdfUseCaseImpl.class}) {
            Arrays.stream(type.getDeclaredMethods()).forEach(method -> Arrays.stream(method.getGenericParameterTypes()).forEach(parameter ->
                    assertFalse(parameter.getTypeName().contains("cashly_api.invoices."), parameter.getTypeName())));
            Arrays.stream(type.getDeclaredFields()).forEach(field ->
                    assertFalse(field.getGenericType().getTypeName().contains("cashly_api.invoices."), field.toString()));
        }
    }
}
