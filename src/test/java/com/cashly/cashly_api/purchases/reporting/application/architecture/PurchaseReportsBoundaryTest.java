package com.cashly.cashly_api.purchases.reporting.application.architecture;
import com.cashly.cashly_api.purchases.reporting.application.ports.out.PurchasePdfPort;
import com.cashly.cashly_api.purchases.reporting.application.ports.out.PurchasesReportPdfPort;
import com.cashly.cashly_api.purchases.reporting.application.generate_purchase_pdf.services.GeneratePurchasePdfUseCaseImpl;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertFalse;
class PurchaseReportsBoundaryTest {
    @Test
    void applicationDoesNotDependOnForeignDtosRepositoriesOrAggregates() {
        for (var type : new Class<?>[]{PurchasePdfPort.class, PurchasesReportPdfPort.class, GeneratePurchasePdfUseCaseImpl.class}) {
            Arrays.stream(type.getDeclaredFields()).forEach(field -> assertFalse(field.getType().getName().matches(".*cashly_api\\.(purchases\\.(?!reporting\\.)|warehouses\\.).*"), field.toString()));
            Arrays.stream(type.getDeclaredMethods()).forEach(method -> Arrays.stream(method.getGenericParameterTypes()).forEach(parameter -> assertFalse(parameter.getTypeName().matches(".*cashly_api.procurement.purchases.(?!reporting\\.).*"), parameter.toString())));
        }
    }
}