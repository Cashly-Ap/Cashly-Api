package com.cashly.cashly_api.sales.quotes.application.architecture;

import com.cashly.cashly_api.sales.quotes.application.create.use_case.CreateQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.products.use_case.GetProductsForQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.get.use_case.GetQuoteByIdUseCase;
import com.cashly.cashly_api.sales.quotes.application.list.use_case.GetQuotesUseCase;
import com.cashly.cashly_api.sales.quotes.application.warehouses.use_case.GetWarehousesForQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.update.use_case.UpdateQuoteUseCase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QuoteModuleBoundaryTest {
    @Test
    void application_use_cases_depend_only_on_quote_owned_types() {
        Class<?>[] useCases = {CreateQuoteUseCase.class, UpdateQuoteUseCase.class, GetQuotesUseCase.class,
                GetQuoteByIdUseCase.class, GetProductsForQuoteUseCase.class, GetWarehousesForQuoteUseCase.class};

        for (Class<?> useCase : useCases) {
            for (var constructor : useCase.getConstructors()) {
                assertTrue(Arrays.stream(constructor.getParameterTypes()).allMatch(type ->
                                type.getPackageName().startsWith("com.cashly.cashly_api.sales.quotes.")),
                        useCase.getSimpleName() + " imports an external module into its constructor");
            }
        }
    }
}
