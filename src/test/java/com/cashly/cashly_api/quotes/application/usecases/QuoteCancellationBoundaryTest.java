package com.cashly.cashly_api.quotes.application.usecases;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertFalse;

class QuoteCancellationBoundaryTest {
    @Test
    void should_depend_on_an_owned_notification_contract() {
        assertFalse(Arrays.stream(CancelQuoteUseCase.class.getDeclaredConstructors())
                .flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
                .anyMatch(type -> type.getPackageName().startsWith("com.cashly.cashly_api.notifications")));
    }
}
