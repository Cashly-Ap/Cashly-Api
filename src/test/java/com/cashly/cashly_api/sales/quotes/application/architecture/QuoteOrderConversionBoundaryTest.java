package com.cashly.cashly_api.sales.quotes.application.architecture;

import com.cashly.cashly_api.sales.quotes.application.convert_to_order.use_case.ConvertQuoteToOrderUseCase;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;

class QuoteOrderConversionBoundaryTest {
    @Test
    void should_orchestrate_conversion_through_owned_contracts() {
        assertThat(Arrays
                .stream(ConvertQuoteToOrderUseCase.class
                        .getDeclaredConstructors())
                .flatMap(constructor -> Arrays
                        .stream(constructor.getParameterTypes()))
                .map(Class::getPackageName))
                .allMatch(name -> name
                        .startsWith("com.cashly.cashly_api.sales.quotes"));
    }
}
