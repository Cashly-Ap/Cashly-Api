package com.cashly.cashly_api.payments.collection.infrastructure.config;
import com.cashly.cashly_api.payments.collection.application.ports.in.PaymentDocumentQuery;
import com.cashly.cashly_api.payments.collection.application.services.PaymentDocumentQueryService;
import com.cashly.cashly_api.payments.collection.application.queries.use_case.GetPaymentByIdUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class PaymentDocumentQueryConfig {
    @Bean
    public PaymentDocumentQuery paymentDocumentQuery(GetPaymentByIdUseCase query) {
        return new PaymentDocumentQueryService(query);
    }
}