package com.parsun.parsun_api.cashregister.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.cashregister.application.usecases.*;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
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
    VerifyRegisterUseCase verifyClosureUseCase(CashRegisterRepository closureRepository) {
        return new VerifyRegisterUseCase(closureRepository);
    }

    @Bean
    GetRegisterHistoryUseCase getClosureHistoryUseCase(CashRegisterRepository closureRepository) {
        return new GetRegisterHistoryUseCase(closureRepository);
    }
}
