package com.cashly.cashly_api.finance.treasury.payments.collection.infrastructure.config;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in.ReturnPaymentOperations;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.services.ReturnPaymentOperationsService;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.services.PaymentAllocationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ReturnPaymentOperationsConfig {
    @Bean
    public ReturnPaymentOperations returnPaymentOperations(PaymentRepository payments, PaymentAllocationRepository allocations, PaymentAllocationService service) {
        return new ReturnPaymentOperationsService(payments, allocations, service);
    }
}
