package com.cashly.cashly_api.cashregister.infrastructure.query;

import com.cashly.cashly_api.cashregister.infrastructure.persistence.CashRegisterJpaRepository;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaPaymentMethodUsageQueryTest {
    @Test
    void should_report_usage_when_owned_records_exist() {
        UUID id = UUID.randomUUID();
        CashRegisterJpaRepository repository = mock(CashRegisterJpaRepository.class);
        when(repository.countDetailsByPaymentMethodId(id)).thenReturn(2L);
        assertTrue(new JpaPaymentMethodUsageQuery(repository).hasUsage(id));
        verify(repository).countDetailsByPaymentMethodId(id);
    }

    @Test
    void should_report_no_usage_when_owned_records_are_absent() {
        UUID id = UUID.randomUUID();
        CashRegisterJpaRepository repository = mock(CashRegisterJpaRepository.class);
        when(repository.countDetailsByPaymentMethodId(id)).thenReturn(0L);
        assertFalse(new JpaPaymentMethodUsageQuery(repository).hasUsage(id));
        verify(repository).countDetailsByPaymentMethodId(id);
    }
}
