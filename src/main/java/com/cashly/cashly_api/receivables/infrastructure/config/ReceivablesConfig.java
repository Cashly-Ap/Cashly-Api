package com.cashly.cashly_api.receivables.infrastructure.config;

import com.cashly.cashly_api.receivables.application.usecases.GetCustomerDebtUseCase;
import com.cashly.cashly_api.receivables.application.usecases.GetCustomerInvoicesUseCase;
import com.cashly.cashly_api.receivables.application.usecases.GetPaymentHistoryUseCase;
import com.cashly.cashly_api.receivables.application.usecases.GetReceivablesSummaryUseCase;
import com.cashly.cashly_api.receivables.application.usecases.SearchCustomerDebtUseCase;
import com.cashly.cashly_api.receivables.domain.repositories.ReceivablesRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReceivablesConfig {

    @Bean
    public GetReceivablesSummaryUseCase getReceivablesSummaryUseCase(ReceivablesRepository receivablesRepository) {
        return new GetReceivablesSummaryUseCase(receivablesRepository);
    }

    @Bean
    public SearchCustomerDebtUseCase searchCustomerDebtUseCase(ReceivablesRepository receivablesRepository) {
        return new SearchCustomerDebtUseCase(receivablesRepository);
    }

    @Bean
    public GetCustomerDebtUseCase getCustomerDebtUseCase(ReceivablesRepository receivablesRepository) {
        return new GetCustomerDebtUseCase(receivablesRepository);
    }

    @Bean
    public GetCustomerInvoicesUseCase getCustomerInvoicesUseCase(ReceivablesRepository receivablesRepository) {
        return new GetCustomerInvoicesUseCase(receivablesRepository);
    }

    @Bean
    public GetPaymentHistoryUseCase getPaymentHistoryUseCase(ReceivablesRepository receivablesRepository) {
        return new GetPaymentHistoryUseCase(receivablesRepository);
    }
}
