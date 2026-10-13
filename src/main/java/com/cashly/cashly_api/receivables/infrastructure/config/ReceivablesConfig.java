package com.cashly.cashly_api.receivables.infrastructure.config;

import com.cashly.cashly_api.receivables.application.usecases.GetCustomerDebtUseCase;
import com.cashly.cashly_api.receivables.application.usecases.GetCustomerInvoicesUseCase;
import com.cashly.cashly_api.receivables.application.usecases.GetPaymentHistoryUseCase;
import com.cashly.cashly_api.receivables.application.usecases.GetReceivablesSummaryUseCase;
import com.cashly.cashly_api.receivables.application.usecases.SearchCustomerDebtUseCase;
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
