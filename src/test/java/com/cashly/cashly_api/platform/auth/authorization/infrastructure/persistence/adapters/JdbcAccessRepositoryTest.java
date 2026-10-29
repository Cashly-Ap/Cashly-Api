package com.cashly.cashly_api.platform.auth.authorization.infrastructure.persistence.adapters;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcAccessRepositoryTest {
    @Test
    void excludes_system_roles_in_the_editing_query() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of());
        assertThat(new JdbcAccessRepository(jdbc).editableRoles()).isEmpty();
        verify(jdbc).query(org.mockito.ArgumentMatchers.eq("select * from auth_roles where system_key IS NULL order by name"), any(RowMapper.class));
    }
}
