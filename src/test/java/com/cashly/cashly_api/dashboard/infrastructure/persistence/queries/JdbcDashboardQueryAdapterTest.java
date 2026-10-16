package com.cashly.cashly_api.dashboard.infrastructure.persistence.queries;

import com.cashly.cashly_api.dashboard.application.dto.response.InvoiceDueResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JdbcDashboardQueryAdapterTest {

    @Mock
    private JdbcTemplate jdbc;

    @Test
    void should_preserve_zero_amounts_for_empty_summary() {
        when(jdbc.queryForObject(any(String.class), org.mockito.ArgumentMatchers.eq(BigDecimal.class)))
                .thenReturn(null);
        when(jdbc.queryForObject(any(String.class), org.mockito.ArgumentMatchers.eq(Long.class)))
                .thenReturn(0L);

        var summary = new JdbcDashboardQueryAdapter(jdbc).getSummary();

        assertThat(summary.monthlySales()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.overdueReceivables()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.pendingOrders()).isZero();
        assertThat(summary.lowStockProducts()).isZero();
    }

    @Test
    void should_map_invoice_due_schedule_without_changing_identity_or_amount() throws Exception {
        UUID invoiceId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDate dueDate = LocalDate.of(2026, 10, 6);
        BigDecimal balance = new BigDecimal("125.50");
        ResultSet row = mock(ResultSet.class);
        when(row.getString("invoice_id")).thenReturn(invoiceId.toString());
        when(row.getString("customer_id")).thenReturn(customerId.toString());
        when(row.getString("invoice_number")).thenReturn("INV-2026-00001");
        when(row.getString("legal_name")).thenReturn("Example Customer");
        when(row.getDate("due_date")).thenReturn(Date.valueOf(dueDate));
        when(row.getBigDecimal("balance")).thenReturn(balance);
        when(jdbc.query(any(String.class), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<InvoiceDueResponse> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(row, 0));
        });

        var result = new JdbcDashboardQueryAdapter(jdbc).getInvoiceDueSchedule();

        assertThat(result).containsExactly(new InvoiceDueResponse(
                invoiceId, "INV-2026-00001", customerId, "Example Customer", dueDate, balance));
    }

    @Test
    void should_propagate_database_failure_without_returning_empty_schedule() {
        var failure = new IllegalStateException("Database query failed");
        when(jdbc.query(any(String.class), any(RowMapper.class))).thenThrow(failure);

        assertThatThrownBy(() -> new JdbcDashboardQueryAdapter(jdbc).getInvoiceDueSchedule())
                .isSameAs(failure);
    }

    @Test
    void should_limit_invoice_due_schedule_to_ten_rows_in_database() {
        when(jdbc.query(any(String.class), any(RowMapper.class))).thenReturn(List.of());

        new JdbcDashboardQueryAdapter(jdbc).getInvoiceDueSchedule();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sqlCaptor.capture(), any(RowMapper.class));

        assertThat(sqlCaptor.getValue()).contains("LIMIT 10");
    }
}
