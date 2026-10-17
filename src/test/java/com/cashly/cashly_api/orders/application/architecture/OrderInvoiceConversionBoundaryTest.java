package com.cashly.cashly_api.orders.application.architecture;

import com.cashly.cashly_api.orders.application.convert_to_invoice.use_case.ConvertToInvoiceUseCase;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;

class OrderInvoiceConversionBoundaryTest {
    @Test
    void should_create_invoices_through_an_owned_contract() {
        assertThat(Arrays.stream(ConvertToInvoiceUseCase.class.getDeclaredConstructors())
                .flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
                .map(Class::getPackageName))
                .noneMatch(name -> name.startsWith("com.cashly.cashly_api.finance.billing.invoices"));
    }
}
