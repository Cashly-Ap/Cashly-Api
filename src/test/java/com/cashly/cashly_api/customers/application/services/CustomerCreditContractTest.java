package com.cashly.cashly_api.customers.application.services;

import com.cashly.cashly_api.customers.application.ports.out.CreditValidationGateway;
import com.cashly.cashly_api.customers.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.invoices.infrastructure.adapters.out.integration.InvoiceCustomerCreditAdapter;
import com.cashly.cashly_api.orders.infrastructure.adapters.out.integration.OrderCustomerCreditAdapter;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CustomerCreditContractTest {
    @Test
    void should_preserve_credit_limits_and_eligibility_through_both_consumer_adapters() {
        UUID customerId = UUID.randomUUID();
        var query = mock(CreditValidationGateway.class);
        var limit = new BigDecimal("1000.00");
        var available = new BigDecimal("400.00");
        when(query.getCreditStatus(customerId)).thenReturn(new CreditStatusResponse(
                customerId, "Customer", "RIF", limit, new BigDecimal("600.00"),
                BigDecimal.ZERO, available, List.of(), false, true));
        var service = new CustomerCreditEligibilityService(query);
        var invoiceAdapter = new InvoiceCustomerCreditAdapter(service);
        var orderAdapter = new OrderCustomerCreditAdapter(service);

        assertThat(invoiceAdapter.isCreditAvailable(customerId, available)).isTrue();
        assertThat(invoiceAdapter.isCreditAvailable(customerId, new BigDecimal("400.01"))).isFalse();
        var limits = orderAdapter.getCreditLimits(customerId);
        assertThat(limits.creditLimit()).isEqualTo(limit);
        assertThat(limits.availableCredit()).isEqualTo(available);
        verify(query, times(3)).getCreditStatus(customerId);
    }
}
