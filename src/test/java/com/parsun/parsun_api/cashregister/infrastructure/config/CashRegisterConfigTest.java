package com.parsun.parsun_api.cashregister.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.cashregister.application.port.CashRegisterExcelPort;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
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
    @Mock private UserRepository userRepository;

    @Test
    void should_create_open_register_use_case() {
        var config = new CashRegisterConfig();
        var useCase = config.openClosureUseCase(repository);
        assertNotNull(useCase);
    }

    @Test
    void should_create_get_active_register_use_case() {
        var config = new CashRegisterConfig();
        var useCase = config.getActiveClosureUseCase(repository, userRepository);
        assertNotNull(useCase);
    }

    @Test
    void should_create_close_register_use_case() {
        var config = new CashRegisterConfig();
        var useCase = config.closeClosureUseCase(repository);
        assertNotNull(useCase);
    }

    @Test
    void should_create_get_history_use_case() {
        var config = new CashRegisterConfig();
        var useCase = config.getClosureHistoryUseCase(repository);
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
        var useCase = config.generateCashRegisterExcelUseCase(repository, excelPort);
        assertNotNull(useCase);
    }
}
