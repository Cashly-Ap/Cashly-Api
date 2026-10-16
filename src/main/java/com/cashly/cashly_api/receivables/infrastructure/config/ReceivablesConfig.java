package com.cashly.cashly_api.receivables.infrastructure.config;

import com.cashly.cashly_api.receivables.application.customer_debt.GetCustomerDebtUseCase;
import com.cashly.cashly_api.receivables.application.customer_invoices.GetCustomerInvoicesUseCase;
import com.cashly.cashly_api.receivables.application.payment_history.GetPaymentHistoryUseCase;
import com.cashly.cashly_api.receivables.application.summary.GetReceivablesSummaryUseCase;
import com.cashly.cashly_api.receivables.application.customer_debt.SearchCustomerDebtUseCase;
import com.cashly.cashly_api.receivables.application.ports.out.ReceivablesQueryGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReceivablesConfig {

    @Bean
    public GetReceivablesSummaryUseCase getReceivablesSummaryUseCase(ReceivablesQueryGateway receivablesRepository) {
        return new GetReceivablesSummaryUseCase(receivablesRepository);
    }

    @Bean
    public SearchCustomerDebtUseCase searchCustomerDebtUseCase(ReceivablesQueryGateway receivablesRepository) {
        return new SearchCustomerDebtUseCase(receivablesRepository);
    }

    @Bean
    public GetCustomerDebtUseCase getCustomerDebtUseCase(ReceivablesQueryGateway receivablesRepository) {
        return new GetCustomerDebtUseCase(receivablesRepository);
    }

    @Bean
    public GetCustomerInvoicesUseCase getCustomerInvoicesUseCase(ReceivablesQueryGateway receivablesRepository) {
        return new GetCustomerInvoicesUseCase(receivablesRepository);
    }

    @Bean
    public GetPaymentHistoryUseCase getPaymentHistoryUseCase(ReceivablesQueryGateway receivablesRepository) {
        return new GetPaymentHistoryUseCase(receivablesRepository);
    }
}
