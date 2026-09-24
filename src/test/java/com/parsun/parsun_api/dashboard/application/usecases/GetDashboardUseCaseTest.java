package com.parsun.parsun_api.dashboard.application.usecases;

import com.parsun.parsun_api.dashboard.application.dto.response.InvoiceDueResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDashboardUseCaseTest {

    @Mock
    private JdbcTemplate jdbc;

    @Test
    void should_limit_invoice_due_schedule_to_ten_rows_in_database() {
        when(jdbc.query(any(String.class), any(RowMapper.class))).thenReturn(List.of());

        new GetDashboardUseCase(jdbc).getInvoiceDueSchedule();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sqlCaptor.capture(), any(RowMapper.class));

        assertThat(sqlCaptor.getValue()).contains("LIMIT 10");
    }
}
