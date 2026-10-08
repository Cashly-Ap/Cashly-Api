package com.cashly.cashly_api.orders.infrastructure.config;

import com.cashly.cashly_api.orders.application.contracts.*;
import com.cashly.cashly_api.orders.application.usecases.*;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class OrderConfigTest {
    @Mock private OrderRepository orders;
    @Mock private OrderCustomerGateway customers;
    @Mock private OrderActorGateway actors;
    @Mock private OrderInventoryGateway inventory;
    @Mock private OrderProductCatalogGateway products;
    @Mock private OrderWarehouseGateway warehouses;
    @Mock private OrderNotificationGateway notifications;
    @Mock private StockAvailabilityGateway stockAvailability;
    @Mock private CustomerCreditGateway credit;
    @Mock private InvoiceCreationGateway invoices;

    @Test
    void createsOrderUseCasesFromContracts() {
        OrderConfig config = new OrderConfig();

        assertNotNull(config.createOrderUseCase(orders, customers, products, actors, inventory));
        assertNotNull(config.getOrderByIdUseCase(orders, stockAvailability, credit));
        assertNotNull(config.getOrdersUseCase(orders, products));
        assertNotNull(config.updateOrderUseCase(orders, products, inventory));
        assertNotNull(config.confirmOrderUseCase(orders, inventory, notifications));
        assertNotNull(config.cancelOrderUseCase(orders, inventory, notifications));
        assertNotNull(config.convertToInvoiceUseCase(orders, invoices, notifications));
        assertNotNull(config.deleteOrderUseCase(orders, inventory));
        assertNotNull(config.assignOrderWarehousesUseCase(orders, inventory, warehouses));
        assertNotNull(config.getProductsForOrderUseCase(customers, products));
    }
}
