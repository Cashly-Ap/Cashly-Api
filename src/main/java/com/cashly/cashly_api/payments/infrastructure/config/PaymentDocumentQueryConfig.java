package com.cashly.cashly_api.payments.infrastructure.config;
import com.cashly.cashly_api.payments.application.api.PaymentDocumentQuery;
import com.cashly.cashly_api.payments.application.services.PaymentDocumentQueryService;
import com.cashly.cashly_api.payments.application.usecases.GetPaymentByIdUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class PaymentDocumentQueryConfig {
    @Bean
    public PaymentDocumentQuery paymentDocumentQuery(GetPaymentByIdUseCase query) {
        return new PaymentDocumentQueryService(query);
    }
}