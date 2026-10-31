package com.cashly.cashly_api.finance.treasury.receivables.infrastructure.config;
import com.cashly.cashly_api.finance.treasury.receivables.application.ports.in.AccountsReceivableReportQuery;
import com.cashly.cashly_api.finance.treasury.receivables.application.services.AccountsReceivableReportQueryService;
import com.cashly.cashly_api.finance.treasury.receivables.application.ports.out.ReceivablesQueryGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class AccountsReceivableReportQueryConfig {
    @Bean
    public AccountsReceivableReportQuery accountsReceivableReportQuery(
            ReceivablesQueryGateway query) {
        return new AccountsReceivableReportQueryService(query);
    }
}