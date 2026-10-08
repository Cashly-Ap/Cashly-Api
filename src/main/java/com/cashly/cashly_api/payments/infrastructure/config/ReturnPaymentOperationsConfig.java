package com.cashly.cashly_api.payments.infrastructure.config;
import com.cashly.cashly_api.payments.application.api.ReturnPaymentOperations;
import com.cashly.cashly_api.payments.application.services.ReturnPaymentOperationsService;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
import com.cashly.cashly_api.payments.domain.services.PaymentAllocationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ReturnPaymentOperationsConfig {
    @Bean
    public ReturnPaymentOperations returnPaymentOperations(PaymentRepository payments, PaymentAllocationRepository allocations, PaymentAllocationService service) {
        return new ReturnPaymentOperationsService(payments, allocations, service);
    }
}
