package com.cashly.cashly_api.quotes.application.usecases;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;

class QuoteInvoiceConversionBoundaryTest {
    @Test
    void should_create_invoices_through_owned_conversion_contracts() {
        assertThat(Arrays.stream(ConvertQuoteToInvoiceUseCase.class.getDeclaredConstructors())
                .flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
                .map(Class::getPackageName))
                .allMatch(name -> name.startsWith("com.cashly.cashly_api.quotes"));
    }
}
