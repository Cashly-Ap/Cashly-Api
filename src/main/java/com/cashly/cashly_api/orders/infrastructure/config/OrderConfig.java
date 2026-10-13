package com.cashly.cashly_api.orders.infrastructure.config;

import com.cashly.cashly_api.orders.application.ports.out.InvoiceCreationGateway;
import com.cashly.cashly_api.orders.application.usecases.*;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.application.ports.out.StockAvailabilityGateway;
import com.cashly.cashly_api.orders.application.ports.out.CustomerCreditGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfig {

    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderRepository orderRepository,
            com.cashly.cashly_api.orders.application.ports.out.OrderCustomerGateway customers,
            com.cashly.cashly_api.orders.application.ports.out.OrderProductCatalogGateway products,
            com.cashly.cashly_api.orders.application.ports.out.OrderActorGateway actors,
            com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway inventory) {
        return new CreateOrderUseCase(orderRepository, customers, products, actors, inventory);
    }

    @Bean
    public GetOrderByIdUseCase getOrderByIdUseCase(OrderRepository orderRepository,
                                                      StockAvailabilityGateway stockEnrichmentService,
                                                      CustomerCreditGateway creditGuard) {
        return new GetOrderByIdUseCase(orderRepository, stockEnrichmentService, creditGuard);
    }

    @Bean
    public GetOrdersUseCase getOrdersUseCase(OrderRepository orderRepository,
                                              com.cashly.cashly_api.orders.application.ports.out.OrderProductCatalogGateway products) {
        return new GetOrdersUseCase(orderRepository, products);
    }

    @Bean
    public UpdateOrderUseCase updateOrderUseCase(OrderRepository orderRepository,
            com.cashly.cashly_api.orders.application.ports.out.OrderProductCatalogGateway products,
            com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway inventory) {
        return new UpdateOrderUseCase(orderRepository, products, inventory);
    }

    @Bean
    public ConfirmOrderUseCase confirmOrderUseCase(OrderRepository orderRepository,
            com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway inventory,
            com.cashly.cashly_api.orders.application.ports.out.OrderNotificationGateway notifications) {
        return new ConfirmOrderUseCase(orderRepository, inventory, notifications);
    }

    @Bean
    public CancelOrderUseCase cancelOrderUseCase(OrderRepository orderRepository,
            com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway inventory,
            com.cashly.cashly_api.orders.application.ports.out.OrderNotificationGateway notifications) {
        return new CancelOrderUseCase(orderRepository, inventory, notifications);
    }

    @Bean
    public ConvertToInvoiceUseCase convertToInvoiceUseCase(OrderRepository orderRepository,
                                                             InvoiceCreationGateway invoiceCreationGateway,
            com.cashly.cashly_api.orders.application.ports.out.OrderNotificationGateway notifications) {
        return new ConvertToInvoiceUseCase(orderRepository, invoiceCreationGateway, notifications);
    }

    @Bean
    public DeleteOrderUseCase deleteOrderUseCase(OrderRepository orderRepository,
            com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway inventory) {
        return new DeleteOrderUseCase(orderRepository, inventory);
    }

    @Bean
    public AssignOrderWarehousesUseCase assignOrderWarehousesUseCase(
            OrderRepository orderRepository,
            com.cashly.cashly_api.orders.application.ports.out.OrderInventoryGateway inventory,
            com.cashly.cashly_api.orders.application.ports.out.OrderWarehouseGateway warehouses) {
        return new AssignOrderWarehousesUseCase(orderRepository, inventory, warehouses);
    }

    @Bean
    public GetProductsForOrderUseCase getProductsForOrderUseCase(
            com.cashly.cashly_api.orders.application.ports.out.OrderCustomerGateway customers,
            com.cashly.cashly_api.orders.application.ports.out.OrderProductCatalogGateway products) {
        return new GetProductsForOrderUseCase(customers, products);
    }
}
