package com.cashly.cashly_api.finance.treasury.receivables.infrastructure.config;

import com.cashly.cashly_api.finance.treasury.receivables.application.customer_debt.use_case.GetCustomerDebtUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.application.customer_invoices.use_case.GetCustomerInvoicesUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.application.payment_history.use_case.GetPaymentHistoryUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.application.summary.use_case.GetReceivablesSummaryUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.application.customer_debt.use_case.SearchCustomerDebtUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.application.ports.out.ReceivablesQueryGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReceivablesConfig {

    @Bean
    public GetReceivablesSummaryUseCase getReceivablesSummaryUseCase(
            ReceivablesQueryGateway receivablesRepository) {
        return new GetReceivablesSummaryUseCase(receivablesRepository);
    }

    @Bean
    public SearchCustomerDebtUseCase searchCustomerDebtUseCase(
            ReceivablesQueryGateway receivablesRepository) {
        return new SearchCustomerDebtUseCase(receivablesRepository);
    }

    @Bean
    public GetCustomerDebtUseCase getCustomerDebtUseCase(
            ReceivablesQueryGateway receivablesRepository) {
        return new GetCustomerDebtUseCase(receivablesRepository);
    }

    @Bean
    public GetCustomerInvoicesUseCase getCustomerInvoicesUseCase(
            ReceivablesQueryGateway receivablesRepository) {
        return new GetCustomerInvoicesUseCase(receivablesRepository);
    }

    @Bean
    public GetPaymentHistoryUseCase getPaymentHistoryUseCase(
            ReceivablesQueryGateway receivablesRepository) {
        return new GetPaymentHistoryUseCase(receivablesRepository);
    }
}
