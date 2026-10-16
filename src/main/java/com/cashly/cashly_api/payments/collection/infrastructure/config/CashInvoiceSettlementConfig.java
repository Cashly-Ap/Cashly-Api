package com.cashly.cashly_api.payments.collection.infrastructure.config;
import com.cashly.cashly_api.payments.collection.application.ports.in.CashInvoiceSettlement;
import com.cashly.cashly_api.payments.collection.application.services.CashInvoiceSettlementService;
import com.cashly.cashly_api.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.payments.collection.domain.PaymentRepository;
import com.cashly.cashly_api.payments.collection.domain.PaymentAllocationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CashInvoiceSettlementConfig {
    @Bean
    public CashInvoiceSettlement cashInvoiceSettlement(PaymentRepository payments, PaymentAllocationRepository allocations, CashRegisterGateway cashRegister) {
        return new CashInvoiceSettlementService(payments, allocations, cashRegister);
    }
}
