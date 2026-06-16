package com.parsun.parsun_api.cashregister.infrastructure.persistence;

import com.parsun.parsun_api.cashregister.application.dto.DailyTotals;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashRegisterRepositoryAdapter Unit Tests")
class CashRegisterRepositoryAdapterTest {

    @Mock
    private CashRegisterJpaRepository jpaRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query nativeQuery;

    private CashRegisterRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CashRegisterRepositoryAdapter(jpaRepository, entityManager);
    }

    @Test
    @DisplayName("should include REFUND balance_transactions in totalReturns")
    void should_include_refund_transactions_in_total_returns() {
        var today = LocalDate.now();

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter("date", today)).thenReturn(nativeQuery);

        when(nativeQuery.getSingleResult())
                .thenReturn(BigDecimal.ZERO)
                .thenReturn(0)
                .thenReturn(BigDecimal.ZERO)
                .thenReturn(0)
                .thenReturn(BigDecimal.ZERO)
                .thenReturn(0)
                .thenReturn(BigDecimal.valueOf(100.00))
                .thenReturn(1)
                .thenReturn(BigDecimal.ZERO)
                .thenReturn(0)
                .thenReturn(BigDecimal.ZERO)
                .thenReturn(BigDecimal.ZERO);

        when(nativeQuery.getResultList()).thenReturn(List.of());

        var result = adapter.findDailyTotals(today);

        assertThat(result.totalReturns()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.returnCount()).isEqualTo(1);
        assertThat(result.totalExpenses()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
