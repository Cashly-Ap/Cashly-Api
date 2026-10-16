package com.cashly.cashly_api.purchases.infrastructure.config;

import com.cashly.cashly_api.purchases.application.ports.in.PurchaseDocumentQuery;
import com.cashly.cashly_api.purchases.application.queries.PurchaseDocumentQueryService;
import com.cashly.cashly_api.purchases.application.list.ListPurchasesUseCase;
import com.cashly.cashly_api.purchases.domain.PurchaseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseDocumentQueryConfig {
    @Bean
    public PurchaseDocumentQuery purchaseDocumentQuery(PurchaseRepository repository, ListPurchasesUseCase listQuery) {
        return new PurchaseDocumentQueryService(repository, listQuery);
    }
}
