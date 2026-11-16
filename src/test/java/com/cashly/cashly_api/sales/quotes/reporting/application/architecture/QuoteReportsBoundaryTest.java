package com.cashly.cashly_api.sales.quotes.reporting.application.architecture;
import com.cashly.cashly_api.sales.quotes.reporting.application.ports.out.QuotePdfPort;
import com.cashly.cashly_api.sales.quotes.reporting.application.ports.out.QuotesReportPdfPort;
import com.cashly.cashly_api.sales.quotes.reporting.application.generate_quote_pdf.services.GenerateQuotePdfUseCaseImpl;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertFalse;
class QuoteReportsBoundaryTest {
    @Test
    void applicationDoesNotDependOnForeignDtosRepositoriesOrAggregates() {
        for (var type : new Class<?>[]{QuotePdfPort.class,
                QuotesReportPdfPort.class, GenerateQuotePdfUseCaseImpl.class}) {
            Arrays.stream(type.getDeclaredFields()).forEach(
                    field -> assertFalse(field.getType().getName().matches(
                            ".*cashly_api\\.(quotes\\.(?!reporting\\.)|warehouses\\.).*"),
                            field.toString()));
            Arrays.stream(type.getDeclaredMethods()).forEach(method -> Arrays
                    .stream(method.getGenericParameterTypes())
                    .forEach(parameter -> assertFalse(
                            parameter.getTypeName().matches(
                                    ".*cashly_api.sales.quotes.(?!reporting\\.).*"),
                            parameter.toString())));
        }
    }
}