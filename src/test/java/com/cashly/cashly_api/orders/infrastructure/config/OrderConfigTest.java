package com.cashly.cashly_api.orders.infrastructure.config;

import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.invoices.application.usecases.CreateInvoiceUseCase;
import com.cashly.cashly_api.orders.application.usecases.*;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.shared.application.StockEnrichmentService;
import com.cashly.cashly_api.shared.services.CreditGuard;
import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderConfig Unit Tests")
class OrderConfigTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private WarehouseStockRepository warehouseStockRepository;
    @Mock private StockEnrichmentService stockEnrichmentService;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private CreateInvoiceUseCase createInvoiceUseCase;
    @Mock private CustomerWarehouseAccessRepository customerWarehouseAccessRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private CreditGuard creditGuard;
    @Mock private NotificationService notificationService;

    @Test
    void should_CreateCreateOrderUseCase() {
        OrderConfig config = new OrderConfig();

        CreateOrderUseCase useCase = config.createOrderUseCase(
                orderRepository, customerRepository, productRepository,
                userRepository, warehouseStockRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetOrderByIdUseCase() {
        OrderConfig config = new OrderConfig();

        GetOrderByIdUseCase useCase = config.getOrderByIdUseCase(
                orderRepository, stockEnrichmentService, customerRepository, creditGuard);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetOrdersUseCase() {
        OrderConfig config = new OrderConfig();

        GetOrdersUseCase useCase = config.getOrdersUseCase(orderRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateUpdateOrderUseCase() {
        OrderConfig config = new OrderConfig();

        UpdateOrderUseCase useCase = config.updateOrderUseCase(
                orderRepository, productRepository, warehouseStockRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateConfirmOrderUseCase() {
        OrderConfig config = new OrderConfig();

        ConfirmOrderUseCase useCase = config.confirmOrderUseCase(
                orderRepository, warehouseStockRepository, stockMovementRepository, notificationService);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateCancelOrderUseCase() {
        OrderConfig config = new OrderConfig();

        CancelOrderUseCase useCase = config.cancelOrderUseCase(
                orderRepository, warehouseStockRepository, stockMovementRepository, notificationService);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateConvertToInvoiceUseCase() {
        OrderConfig config = new OrderConfig();

        ConvertToInvoiceUseCase useCase = config.convertToInvoiceUseCase(
                orderRepository, createInvoiceUseCase, notificationService);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateDeleteOrderUseCase() {
        OrderConfig config = new OrderConfig();

        DeleteOrderUseCase useCase = config.deleteOrderUseCase(
                orderRepository, warehouseStockRepository);

        assertNotNull(useCase);
    }

    @Test
    void should_CreateGetProductsForOrderUseCase() {
        OrderConfig config = new OrderConfig();

        GetProductsForOrderUseCase useCase = config.getProductsForOrderUseCase(
                customerRepository, customerWarehouseAccessRepository,
                productRepository, categoryRepository,
                warehouseStockRepository, warehouseRepository);

        assertNotNull(useCase);
    }
}
