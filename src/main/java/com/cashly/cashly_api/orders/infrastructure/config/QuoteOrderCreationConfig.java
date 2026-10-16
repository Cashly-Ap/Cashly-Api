package com.cashly.cashly_api.orders.infrastructure.config;

import com.cashly.cashly_api.orders.application.ports.in.QuoteOrderCreationApi;
import com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.orders.application.create.from_quote.services.QuoteOrderCreationService;
import com.cashly.cashly_api.orders.domain.repository.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuoteOrderCreationConfig {
    @Bean
    public QuoteOrderCreationApi quoteOrderCreationApi(OrderRepository orders, OrderInventoryGateway inventory) {
        return new QuoteOrderCreationService(orders, inventory);
    }
}
