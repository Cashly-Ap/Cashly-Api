package com.cashly.cashly_api.quotes.application;

import com.cashly.cashly_api.quotes.application.usecases.AssignQuoteWarehousesUseCase;
import com.cashly.cashly_api.quotes.application.usecases.CreateQuoteUseCase;
import com.cashly.cashly_api.quotes.application.usecases.GetProductsForQuoteUseCase;
import com.cashly.cashly_api.quotes.application.usecases.GetQuoteByIdUseCase;
import com.cashly.cashly_api.quotes.application.usecases.GetQuotesUseCase;
import com.cashly.cashly_api.quotes.application.usecases.GetWarehousesForQuoteUseCase;
import com.cashly.cashly_api.quotes.application.usecases.UpdateQuoteUseCase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QuoteModuleBoundaryTest {
    @Test
    void application_use_cases_depend_only_on_quote_owned_types() {
        Class<?>[] useCases = {CreateQuoteUseCase.class, UpdateQuoteUseCase.class, GetQuotesUseCase.class,
                GetQuoteByIdUseCase.class, GetProductsForQuoteUseCase.class, GetWarehousesForQuoteUseCase.class,
                AssignQuoteWarehousesUseCase.class};

        for (Class<?> useCase : useCases) {
            for (var constructor : useCase.getConstructors()) {
                assertTrue(Arrays.stream(constructor.getParameterTypes()).allMatch(type ->
                                type.getPackageName().startsWith("com.cashly.cashly_api.quotes.")),
                        useCase.getSimpleName() + " imports an external module into its constructor");
            }
        }
    }
}
