package com.cashly.cashly_api.payments.infrastructure.config;
import com.cashly.cashly_api.payments.application.ports.in.CashInvoiceSettlement;
import com.cashly.cashly_api.payments.application.services.CashInvoiceSettlementService;
import com.cashly.cashly_api.payments.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CashInvoiceSettlementConfig {
    @Bean
    public CashInvoiceSettlement cashInvoiceSettlement(PaymentRepository payments, PaymentAllocationRepository allocations, CashRegisterGateway cashRegister) {
        return new CashInvoiceSettlementService(payments, allocations, cashRegister);
    }
}
