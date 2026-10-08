package com.cashly.cashly_api.receivables.infrastructure.integration;

import com.cashly.cashly_api.customers.application.api.CustomerProfileQuery;
import com.cashly.cashly_api.customers.application.api.CustomerProfileSnapshot;
import com.cashly.cashly_api.customers.application.exceptions.CustomerNotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalReceivablesCustomerProfileGatewayAdapterTest {
    private final CustomerProfileQuery customerProfiles = mock(CustomerProfileQuery.class);
    private final LocalReceivablesCustomerProfileGatewayAdapter adapter =
            new LocalReceivablesCustomerProfileGatewayAdapter(customerProfiles);

    @Test
    void shouldReturnReceivablesOwnedSnapshotFromCustomerProfile() {
        UUID customerId = UUID.randomUUID();
        when(customerProfiles.find(customerId)).thenReturn(new CustomerProfileSnapshot(customerId, 7L,
                "Customer", "COMPANY", "J-1", null, null, 30, BigDecimal.valueOf(500),
                BigDecimal.valueOf(25), true, null, null, List.of("a@cashly.test"), List.of("555"), List.of()));

        var profile = adapter.findById(customerId).orElseThrow();

        assertThat(profile.legalName()).isEqualTo("Customer");
        assertThat(profile.rif()).isEqualTo("J-1");
        assertThat(profile.creditLimit()).isEqualByComparingTo("500");
        assertThat(profile.currentBalance()).isEqualByComparingTo("25");
        assertThat(profile.phones()).containsExactly("555");
        assertThat(profile.emails()).containsExactly("a@cashly.test");
    }

    @Test
    void shouldRepresentMissingCustomerAsEmpty() {
        UUID customerId = UUID.randomUUID();
        when(customerProfiles.find(customerId)).thenThrow(new CustomerNotFoundException("missing"));

        assertThat(adapter.findById(customerId)).isEmpty();
    }

    @Test
    void shouldPropagateUnexpectedCustomerQueryFailure() {
        UUID customerId = UUID.randomUUID();
        IllegalStateException failure = new IllegalStateException("database unavailable");
        when(customerProfiles.find(customerId)).thenThrow(failure);

        assertThatThrownBy(() -> adapter.findById(customerId)).isSameAs(failure);
    }
}
