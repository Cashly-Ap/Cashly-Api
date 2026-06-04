package com.parsun.parsun_api.advances.infrastructure.config;

import com.parsun.parsun_api.advances.application.usecases.*;
import com.parsun.parsun_api.advances.domain.repositories.AdvanceConsumptionRepository;
import com.parsun.parsun_api.advances.domain.repositories.AdvanceRepository;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdvanceConfig {

    @Bean
    public CreateAdvanceUseCase createAdvanceUseCase(AdvanceRepository advanceRepository,
                                                      CustomerRepository customerRepository,
                                                      UserRepository userRepository) {
        return new CreateAdvanceUseCase(advanceRepository, customerRepository, userRepository);
    }

    @Bean
    public GetAllAdvancesUseCase getAllAdvancesUseCase(AdvanceRepository advanceRepository) {
        return new GetAllAdvancesUseCase(advanceRepository);
    }

    @Bean
    public GetAdvanceByIdUseCase getAdvanceByIdUseCase(AdvanceRepository advanceRepository,
                                                        AdvanceConsumptionRepository consumptionRepository) {
        return new GetAdvanceByIdUseCase(advanceRepository, consumptionRepository);
    }

    @Bean
    public ApplyAdvanceUseCase applyAdvanceUseCase(AdvanceRepository advanceRepository,
                                                    AdvanceConsumptionRepository consumptionRepository) {
        return new ApplyAdvanceUseCase(advanceRepository, consumptionRepository);
    }

    @Bean
    public CancelAdvanceUseCase cancelAdvanceUseCase(AdvanceRepository advanceRepository) {
        return new CancelAdvanceUseCase(advanceRepository);
    }
}
