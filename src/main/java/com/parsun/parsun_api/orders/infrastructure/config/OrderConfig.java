package com.parsun.parsun_api.orders.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.invoices.application.usecases.CreateInvoiceUseCase;
import com.parsun.parsun_api.orders.application.usecases.*;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.shared.application.StockEnrichmentService;
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
                                                    WarehouseStockRepository warehouseStockRepository) {
        return new CreateOrderUseCase(orderRepository, customerRepository, productRepository,
                userRepository, warehouseStockRepository);
    }

    @Bean
    public GetOrderByIdUseCase getOrderByIdUseCase(OrderRepository orderRepository,
                                                      StockEnrichmentService stockEnrichmentService) {
        return new GetOrderByIdUseCase(orderRepository, stockEnrichmentService);
    }

    @Bean
    public GetOrdersUseCase getOrdersUseCase(OrderRepository orderRepository) {
        return new GetOrdersUseCase(orderRepository);
    }

    @Bean
    public UpdateOrderUseCase updateOrderUseCase(OrderRepository orderRepository,
                                                    ProductRepository productRepository,
                                                    WarehouseStockRepository warehouseStockRepository) {
        return new UpdateOrderUseCase(orderRepository, productRepository,
                warehouseStockRepository);
    }

    @Bean
    public ConfirmOrderUseCase confirmOrderUseCase(OrderRepository orderRepository,
                                                     WarehouseStockRepository warehouseStockRepository,
                                                     StockMovementRepository stockMovementRepository) {
        return new ConfirmOrderUseCase(orderRepository, warehouseStockRepository, stockMovementRepository);
    }

    @Bean
    public CancelOrderUseCase cancelOrderUseCase(OrderRepository orderRepository,
                                                   WarehouseStockRepository warehouseStockRepository,
                                                   StockMovementRepository stockMovementRepository) {
        return new CancelOrderUseCase(orderRepository, warehouseStockRepository, stockMovementRepository);
    }

    @Bean
    public ConvertToInvoiceUseCase convertToInvoiceUseCase(OrderRepository orderRepository,
                                                             CreateInvoiceUseCase createInvoiceUseCase) {
        return new ConvertToInvoiceUseCase(orderRepository, createInvoiceUseCase);
    }

    @Bean
    public DeleteOrderUseCase deleteOrderUseCase(OrderRepository orderRepository,
                                                   WarehouseStockRepository warehouseStockRepository) {
        return new DeleteOrderUseCase(orderRepository, warehouseStockRepository);
    }

    @Bean
    public AssignOrderWarehousesUseCase assignOrderWarehousesUseCase(
            OrderRepository orderRepository,
            WarehouseStockRepository warehouseStockRepository,
            CustomerWarehouseAccessRepository customerWarehouseAccessRepository) {
        return new AssignOrderWarehousesUseCase(
                orderRepository, warehouseStockRepository, customerWarehouseAccessRepository);
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
