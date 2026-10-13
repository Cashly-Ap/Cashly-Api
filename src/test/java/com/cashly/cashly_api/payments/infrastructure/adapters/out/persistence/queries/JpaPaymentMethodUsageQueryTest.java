package com.cashly.cashly_api.payments.infrastructure.adapters.out.persistence.queries;

import com.cashly.cashly_api.payments.infrastructure.adapters.out.persistence.repositories.PaymentJpaRepository;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaPaymentMethodUsageQueryTest {
    @Test
    void should_report_usage_when_owned_records_exist() {
        UUID id = UUID.randomUUID();
        PaymentJpaRepository repository = mock(PaymentJpaRepository.class);
        when(repository.countByPaymentMethodId(id)).thenReturn(2L);
        assertTrue(new JpaPaymentMethodUsageQuery(repository).hasUsage(id));
        verify(repository).countByPaymentMethodId(id);
    }

    @Test
    void should_report_no_usage_when_owned_records_are_absent() {
        UUID id = UUID.randomUUID();
        PaymentJpaRepository repository = mock(PaymentJpaRepository.class);
        when(repository.countByPaymentMethodId(id)).thenReturn(0L);
        assertFalse(new JpaPaymentMethodUsageQuery(repository).hasUsage(id));
        verify(repository).countByPaymentMethodId(id);
    }
}
