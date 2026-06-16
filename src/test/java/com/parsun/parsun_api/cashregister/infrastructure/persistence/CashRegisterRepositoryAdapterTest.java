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
import java.util.UUID;

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

    @Test
    @DisplayName("should find payment method summary with totals grouped by method")
    void should_find_payment_method_summary() {
        var dateFrom = LocalDate.now().minusDays(1);
        var dateTo = LocalDate.now();

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter("dateFrom", dateFrom)).thenReturn(nativeQuery);
        when(nativeQuery.setParameter("dateTo", dateTo)).thenReturn(nativeQuery);

        Object[] efectivoRow = {UUID.randomUUID(), "Efectivo", 1500.0, 3};
        Object[] dolarRow = {UUID.randomUUID(), "Dolar", 2000.0, 2};
        when(nativeQuery.getResultList()).thenReturn(List.of(efectivoRow, dolarRow));

        var result = adapter.findPaymentMethodSummary(dateFrom, dateTo);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPaymentMethodName()).isEqualTo("Efectivo");
        assertThat(result.get(0).getExpectedAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(result.get(0).getTransactionCount()).isEqualTo(3);
        assertThat(result.get(1).getPaymentMethodName()).isEqualTo("Dolar");
        assertThat(result.get(1).getExpectedAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(result.get(1).getTransactionCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("should return empty list when no payments for payment method summary")
    void should_return_empty_payment_method_summary_when_no_payments() {
        var dateFrom = LocalDate.now().minusDays(7);
        var dateTo = LocalDate.now();

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter("dateFrom", dateFrom)).thenReturn(nativeQuery);
        when(nativeQuery.setParameter("dateTo", dateTo)).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of());

        var result = adapter.findPaymentMethodSummary(dateFrom, dateTo);

        assertThat(result).isEmpty();
    }
}
