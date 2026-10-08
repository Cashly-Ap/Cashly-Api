package com.cashly.cashly_api.reports.quotes.application;
import com.cashly.cashly_api.reports.quotes.application.port.QuotePdfPort;
import com.cashly.cashly_api.reports.quotes.application.port.QuotesReportPdfPort;
import com.cashly.cashly_api.reports.quotes.application.usecases.GenerateQuotePdfUseCaseImpl;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertFalse;
class QuoteReportsBoundaryTest {
    @Test
    void applicationDoesNotDependOnForeignDtosRepositoriesOrAggregates() {
        for (var type : new Class<?>[]{QuotePdfPort.class, QuotesReportPdfPort.class, GenerateQuotePdfUseCaseImpl.class}) {
            Arrays.stream(type.getDeclaredFields()).forEach(field -> assertFalse(field.getType().getName().matches(".*cashly_api\\.(quotes|warehouses)\\..*"), field.toString()));
            Arrays.stream(type.getDeclaredMethods()).forEach(method -> Arrays.stream(method.getGenericParameterTypes()).forEach(parameter -> assertFalse(parameter.getTypeName().contains("cashly_api.quotes."), parameter.toString())));
        }
    }
}