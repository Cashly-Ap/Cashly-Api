package com.cashly.cashly_api.purchases.infrastructure.config;

import com.cashly.cashly_api.purchases.application.ports.in.PurchaseDocumentQuery;
import com.cashly.cashly_api.purchases.application.queries.services.PurchaseDocumentQueryService;
import com.cashly.cashly_api.purchases.application.list.use_case.ListPurchasesUseCase;
import com.cashly.cashly_api.purchases.domain.repository.PurchaseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchaseDocumentQueryConfig {
    @Bean
    public PurchaseDocumentQuery purchaseDocumentQuery(PurchaseRepository repository, ListPurchasesUseCase listQuery) {
        return new PurchaseDocumentQueryService(repository, listQuery);
    }
}
