package com.cashly.cashly_api.analytics.dashboard.application.ports.out;

import com.cashly.cashly_api.analytics.dashboard.application.summary.use_case.GetDashboardUseCase;

import com.cashly.cashly_api.analytics.dashboard.application.ports.out.DashboardQueryGateway;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.DashboardSummaryResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardQueryGatewayTest {

    private final DashboardQueryGateway gateway = mock(DashboardQueryGateway.class);
    private final GetDashboardUseCase useCase = new GetDashboardUseCase(gateway);

    @Test
    void should_preserve_summary_from_query_gateway() {
        var summary = new DashboardSummaryResponse(BigDecimal.TEN, 2L, BigDecimal.ONE, 3L);
        when(gateway.getSummary()).thenReturn(summary);

        assertThat(useCase.getSummary()).isSameAs(summary);
    }

    @Test
    void should_preserve_empty_monthly_sales() {
        when(gateway.getMonthlySales()).thenReturn(List.of());

        assertThat(useCase.getMonthlySales()).isEmpty();
        verify(gateway).getMonthlySales();
    }

    @Test
    void should_forward_product_limit() {
        int limit = 7;
        when(gateway.getTopProducts(limit)).thenReturn(List.of());

        assertThat(useCase.getTopProducts(limit)).isEmpty();
        verify(gateway).getTopProducts(limit);
    }

    @Test
    void should_forward_customer_limit() {
        int limit = 9;
        when(gateway.getTopCustomers(limit)).thenReturn(List.of());

        assertThat(useCase.getTopCustomers(limit)).isEmpty();
        verify(gateway).getTopCustomers(limit);
    }

    @Test
    void should_preserve_recent_activity() {
        when(gateway.getRecentActivity()).thenReturn(List.of());

        assertThat(useCase.getRecentActivity()).isEmpty();
        verify(gateway).getRecentActivity();
    }

    @Test
    void should_preserve_invoice_due_schedule() {
        when(gateway.getInvoiceDueSchedule()).thenReturn(List.of());

        assertThat(useCase.getInvoiceDueSchedule()).isEmpty();
        verify(gateway).getInvoiceDueSchedule();
    }

    @Test
    void should_propagate_query_failure_instead_of_returning_false_financial_data() {
        var failure = new IllegalStateException("Dashboard query unavailable");
        when(gateway.getSummary()).thenThrow(failure);

        assertThatThrownBy(useCase::getSummary).isSameAs(failure);
    }
}
