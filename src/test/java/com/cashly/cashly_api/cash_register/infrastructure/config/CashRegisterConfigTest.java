package com.cashly.cashly_api.cash_register.infrastructure.config;

import com.cashly.cashly_api.cash_register.reporting.application.ports.out.CashRegisterExcelPort;
import com.cashly.cashly_api.cash_register.application.ports.out.CashRegisterQueryGateway;
import com.cashly.cashly_api.cash_register.sessions.domain.CashRegisterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashRegisterConfig Unit Tests")
class CashRegisterConfigTest {

    @Mock private CashRegisterRepository repository;
    @Mock private CashRegisterQueryGateway queries;

    @Test
    void should_create_open_register_use_case() {
        var config = new CashRegisterConfig();
        var useCase = config.openClosureUseCase(repository);
        assertNotNull(useCase);
    }

    @Test
    void should_create_get_active_register_use_case() {
        var config = new CashRegisterConfig();
        var useCase = config.getActiveClosureUseCase(repository, queries);
        assertNotNull(useCase);
    }

    @Test
    void should_create_close_register_use_case() {
        var config = new CashRegisterConfig();
        var useCase = config.closeClosureUseCase(repository, queries);
        assertNotNull(useCase);
    }

    @Test
    void should_create_get_history_use_case() {
        var config = new CashRegisterConfig();
        var useCase = config.getClosureHistoryUseCase(repository, queries);
        assertNotNull(useCase);
    }

    @Test
    void should_create_excel_port() {
        var config = new CashRegisterConfig();
        CashRegisterExcelPort port = config.cashRegisterExcelPort();
        assertNotNull(port);
    }

    @Test
    void should_create_generate_excel_use_case() {
        var config = new CashRegisterConfig();
        var excelPort = config.cashRegisterExcelPort();
        var useCase = config.generateCashRegisterExcelUseCase(queries, excelPort);
        assertNotNull(useCase);
    }
}
