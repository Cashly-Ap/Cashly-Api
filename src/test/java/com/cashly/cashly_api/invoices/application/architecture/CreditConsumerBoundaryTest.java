package com.cashly.cashly_api.invoices.application.architecture;

import com.cashly.cashly_api.invoices.application.services.InvoiceAuthorizationService;
import com.cashly.cashly_api.orders.application.get.GetOrderByIdUseCase;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

class CreditConsumerBoundaryTest {
    @Test
    void should_depend_on_owned_credit_contracts_instead_of_shared_business_services() {
        assertThat(Stream.of(InvoiceAuthorizationService.class, GetOrderByIdUseCase.class)
                .flatMap(type -> Arrays.stream(type.getDeclaredConstructors()))
                .flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
                .map(Class::getName))
                .noneMatch(name -> name.equals("com.cashly.cashly_api.shared.services.CreditGuard"));
    }
}
