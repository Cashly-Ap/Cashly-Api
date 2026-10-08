package com.cashly.cashly_api.orders.infrastructure.config;

import com.cashly.cashly_api.orders.application.api.OrderDocumentQuery;
import com.cashly.cashly_api.orders.application.services.OrderDocumentQueryService;
import com.cashly.cashly_api.orders.application.usecases.GetOrdersUseCase;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderDocumentQueryConfig {
    @Bean
    public OrderDocumentQuery orderDocumentQuery(OrderRepository repository, GetOrdersUseCase listQuery) {
        return new OrderDocumentQueryService(repository, listQuery);
    }
}
