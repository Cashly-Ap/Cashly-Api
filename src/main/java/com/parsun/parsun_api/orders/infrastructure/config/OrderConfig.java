package com.parsun.parsun_api.orders.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.orders.application.usecases.*;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfig {

    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderRepository orderRepository,
                                                   CustomerRepository customerRepository,
                                                   ProductRepository productRepository,
                                                   UserRepository userRepository,
                                                   WarehouseStockRepository warehouseStockRepository,
                                                   StockMovementRepository stockMovementRepository) {
        return new CreateOrderUseCase(orderRepository, customerRepository, productRepository,
                userRepository, warehouseStockRepository, stockMovementRepository);
    }

    @Bean
    public GetOrderByIdUseCase getOrderByIdUseCase(OrderRepository orderRepository) {
        return new GetOrderByIdUseCase(orderRepository);
    }

    @Bean
    public GetOrdersUseCase getOrdersUseCase(OrderRepository orderRepository) {
        return new GetOrdersUseCase(orderRepository);
    }

    @Bean
    public UpdateOrderUseCase updateOrderUseCase(OrderRepository orderRepository,
                                                   ProductRepository productRepository) {
        return new UpdateOrderUseCase(orderRepository, productRepository);
    }

    @Bean
    public ConfirmOrderUseCase confirmOrderUseCase(OrderRepository orderRepository) {
        return new ConfirmOrderUseCase(orderRepository);
    }

    @Bean
    public CancelOrderUseCase cancelOrderUseCase(OrderRepository orderRepository) {
        return new CancelOrderUseCase(orderRepository);
    }

    @Bean
    public ConvertToInvoiceUseCase convertToInvoiceUseCase(OrderRepository orderRepository) {
        return new ConvertToInvoiceUseCase(orderRepository);
    }

    @Bean
    public DeleteOrderUseCase deleteOrderUseCase(OrderRepository orderRepository) {
        return new DeleteOrderUseCase(orderRepository);
    }

    @Bean
    public GetProductsForOrderUseCase getProductsForOrderUseCase(
            CustomerRepository customerRepository,
            CustomerWarehouseAccessRepository customerWarehouseAccessRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            WarehouseStockRepository warehouseStockRepository,
            WarehouseRepository warehouseRepository) {
        return new GetProductsForOrderUseCase(
                customerRepository,
                customerWarehouseAccessRepository,
                productRepository,
                categoryRepository,
                warehouseStockRepository,
                warehouseRepository);
    }
}
