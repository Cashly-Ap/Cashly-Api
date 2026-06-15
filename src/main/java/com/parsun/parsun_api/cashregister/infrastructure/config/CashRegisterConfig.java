package com.parsun.parsun_api.cashregister.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.cashregister.application.port.CashRegisterExcelPort;
import com.parsun.parsun_api.cashregister.application.usecases.CloseRegisterUseCase;
import com.parsun.parsun_api.cashregister.application.usecases.GenerateCashRegisterExcelUseCase;
import com.parsun.parsun_api.cashregister.application.usecases.GenerateCashRegisterExcelUseCaseImpl;
import com.parsun.parsun_api.cashregister.application.usecases.GetActiveRegisterUseCase;
import com.parsun.parsun_api.cashregister.application.usecases.GetRegisterHistoryUseCase;
import com.parsun.parsun_api.cashregister.application.usecases.OpenRegisterUseCase;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import com.parsun.parsun_api.cashregister.infrastructure.excel.CashRegisterExcelGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CashRegisterConfig {

    @Bean
    OpenRegisterUseCase openClosureUseCase(CashRegisterRepository closureRepository) {
        return new OpenRegisterUseCase(closureRepository);
    }

    @Bean
    GetActiveRegisterUseCase getActiveClosureUseCase(CashRegisterRepository closureRepository,
                                                     UserRepository userRepository) {
        return new GetActiveRegisterUseCase(closureRepository, userRepository);
    }

    @Bean
    CloseRegisterUseCase closeClosureUseCase(CashRegisterRepository closureRepository) {
        return new CloseRegisterUseCase(closureRepository);
    }

    @Bean
    GetRegisterHistoryUseCase getClosureHistoryUseCase(CashRegisterRepository closureRepository) {
        return new GetRegisterHistoryUseCase(closureRepository);
    }

    @Bean
    CashRegisterExcelPort cashRegisterExcelPort() {
        return new CashRegisterExcelGenerator();
    }

    @Bean
    GenerateCashRegisterExcelUseCase generateCashRegisterExcelUseCase(
            CashRegisterRepository repository,
            CashRegisterExcelPort excelPort) {
        return new GenerateCashRegisterExcelUseCaseImpl(repository, excelPort);
    }
}
