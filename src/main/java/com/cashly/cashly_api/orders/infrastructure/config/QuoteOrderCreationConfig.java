package com.cashly.cashly_api.orders.infrastructure.config;

import com.cashly.cashly_api.orders.application.api.QuoteOrderCreationApi;
import com.cashly.cashly_api.orders.application.contracts.OrderInventoryGateway;
import com.cashly.cashly_api.orders.application.services.QuoteOrderCreationService;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuoteOrderCreationConfig {
    @Bean
    public QuoteOrderCreationApi quoteOrderCreationApi(OrderRepository orders, OrderInventoryGateway inventory) {
        return new QuoteOrderCreationService(orders, inventory);
    }
}
