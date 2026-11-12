package com.cashly.cashly_api.finance.treasury.payables.infrastructure.config;

import com.cashly.cashly_api.finance.treasury.payables.application.customer_debt.use_case.GetSupplierDebtUseCase;
import com.cashly.cashly_api.finance.treasury.payables.application.customer_debt.use_case.SearchSupplierDebtUseCase;
import com.cashly.cashly_api.finance.treasury.payables.application.payment_history.use_case.GetPayablePaymentHistoryUseCase;
import com.cashly.cashly_api.finance.treasury.payables.application.ports.out.PayablesQueryGateway;
import com.cashly.cashly_api.finance.treasury.payables.application.summary.use_case.GetPayablesSummaryUseCase;
import com.cashly.cashly_api.finance.treasury.payables.application.supplier_purchases.use_case.GetSupplierPurchasesUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayablesConfig {

    @Bean
    public GetPayablesSummaryUseCase getPayablesSummaryUseCase(
            PayablesQueryGateway gateway) {
        return new GetPayablesSummaryUseCase(gateway);
    }

    @Bean
    public SearchSupplierDebtUseCase searchSupplierDebtUseCase(
            PayablesQueryGateway gateway) {
        return new SearchSupplierDebtUseCase(gateway);
    }

    @Bean
    public GetSupplierDebtUseCase getSupplierDebtUseCase(
            PayablesQueryGateway gateway) {
        return new GetSupplierDebtUseCase(gateway);
    }

    @Bean
    public GetSupplierPurchasesUseCase getSupplierPurchasesUseCase(
            PayablesQueryGateway gateway) {
        return new GetSupplierPurchasesUseCase(gateway);
    }

    @Bean
    public GetPayablePaymentHistoryUseCase getPayablePaymentHistoryUseCase(
            PayablesQueryGateway gateway) {
        return new GetPayablePaymentHistoryUseCase(gateway);
    }
}
