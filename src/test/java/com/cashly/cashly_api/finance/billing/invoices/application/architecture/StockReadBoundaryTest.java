package com.cashly.cashly_api.finance.billing.invoices.application.architecture;

import com.cashly.cashly_api.finance.billing.invoices.application.get.use_case.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.finance.billing.invoices.application.update.use_case.UpdateInvoiceUseCase;

import com.cashly.cashly_api.sales.orders.application.get.use_case.GetOrderByIdUseCase;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

class StockReadBoundaryTest {
    @Test
    void should_read_stock_through_owned_contracts() {
        assertThat(Stream
                .of(GetInvoiceByIdUseCase.class, UpdateInvoiceUseCase.class,
                        GetOrderByIdUseCase.class)
                .flatMap(type -> Arrays.stream(type.getDeclaredConstructors()))
                .flatMap(constructor -> Arrays
                        .stream(constructor.getParameterTypes()))
                .map(Class::getName))
                .noneMatch(name -> name.equals(
                        "com.cashly.cashly_api.shared.application.StockEnrichmentService"));
    }
}
