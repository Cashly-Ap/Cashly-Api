package com.parsun.parsun_api.payment_methods.infrastructure.config;

import com.parsun.parsun_api.payment_methods.application.usecases.*;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payment_methods.infrastructure.persistence.repositories.PaymentMethodJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentMethodConfig {

    @Bean
    public CreatePaymentMethodUseCase createPaymentMethodUseCase(PaymentMethodRepository repository,
                                                                  PaymentMethodJpaRepository jpaRepository) {
        return new CreatePaymentMethodUseCase(repository, jpaRepository);
    }

    @Bean
    public UpdatePaymentMethodUseCase updatePaymentMethodUseCase(PaymentMethodRepository repository) {
        return new UpdatePaymentMethodUseCase(repository);
    }

    @Bean
    public ListPaymentMethodsUseCase listPaymentMethodsUseCase(PaymentMethodRepository repository) {
        return new ListPaymentMethodsUseCase(repository);
    }

    @Bean
    public GetPaymentMethodByIdUseCase getPaymentMethodByIdUseCase(PaymentMethodRepository repository) {
        return new GetPaymentMethodByIdUseCase(repository);
    }

    @Bean
    public TogglePaymentMethodUseCase togglePaymentMethodUseCase(PaymentMethodRepository repository) {
        return new TogglePaymentMethodUseCase(repository);
    }

    @Bean
    public DeletePaymentMethodUseCase deletePaymentMethodUseCase(PaymentMethodRepository repository) {
        return new DeletePaymentMethodUseCase(repository);
    }
}
