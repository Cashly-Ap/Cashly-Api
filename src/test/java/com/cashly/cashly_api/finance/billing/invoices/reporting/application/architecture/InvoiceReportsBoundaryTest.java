package com.cashly.cashly_api.finance.billing.invoices.reporting.application.architecture;

import com.cashly.cashly_api.finance.billing.invoices.reporting.application.ports.out.InvoicePdfPort;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.ports.out.InvoicesReportPdfPort;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.generate_invoice_pdf.services.GenerateInvoicePdfUseCaseImpl;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InvoiceReportsBoundaryTest {
    @Test
    void applicationContractsDoNotExposeInvoiceModuleDtosOrConcreteUseCases() {
        for (Class<?> type : new Class<?>[]{InvoicePdfPort.class,
                InvoicesReportPdfPort.class,
                GenerateInvoicePdfUseCaseImpl.class}) {
            Arrays.stream(type.getDeclaredMethods()).forEach(method -> Arrays
                    .stream(method.getGenericParameterTypes())
                    .forEach(parameter -> assertFalse(
                            parameter.getTypeName().matches(
                                    ".*cashly_api.finance.billing.invoices.(?!reporting\\.).*"),
                            parameter.getTypeName())));
            Arrays.stream(type.getDeclaredFields())
                    .forEach(field -> assertFalse(
                            field.getGenericType().getTypeName().matches(
                                    ".*cashly_api.finance.billing.invoices.(?!reporting\\.).*"),
                            field.toString()));
        }
    }
}
