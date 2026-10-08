package com.cashly.cashly_api.invoices.application.usecases;

import com.cashly.cashly_api.orders.application.usecases.GetOrderByIdUseCase;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

class StockReadBoundaryTest {
    @Test
    void should_read_stock_through_owned_contracts() {
        assertThat(Stream.of(GetInvoiceByIdUseCase.class, UpdateInvoiceUseCase.class, GetOrderByIdUseCase.class)
                .flatMap(type -> Arrays.stream(type.getDeclaredConstructors()))
                .flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
                .map(Class::getName))
                .noneMatch(name -> name.equals("com.cashly.cashly_api.shared.application.StockEnrichmentService"));
    }
}
