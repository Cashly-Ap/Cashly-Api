package com.cashly.cashly_api.sales.orders.infrastructure.config;

import com.cashly.cashly_api.sales.orders.application.ports.out.CustomerCreditGateway;
import com.cashly.cashly_api.sales.orders.application.ports.out.InvoiceCreationGateway;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderActorGateway;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderCustomerGateway;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway;
import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.sales.orders.application.ports.out.StockAvailabilityGateway;

import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class OrderConfigTest {
    @Mock
    private OrderRepository orders;
    @Mock
    private OrderCustomerGateway customers;
    @Mock
    private OrderActorGateway actors;
    @Mock
    private OrderInventoryGateway inventory;
    @Mock
    private OrderProductCatalogGateway products;
    @Mock
    private ActivityNotificationRecorder activityNotifications;
    @Mock
    private StockAvailabilityGateway stockAvailability;
    @Mock
    private CustomerCreditGateway credit;
    @Mock
    private InvoiceCreationGateway invoices;

    @Test
    void createsOrderUseCasesFromContracts() {
        OrderConfig config = new OrderConfig();

        assertNotNull(config.createOrderUseCase(orders, customers, products,
                actors, inventory, activityNotifications));
        assertNotNull(
                config.getOrderByIdUseCase(orders, stockAvailability, credit));
        assertNotNull(config.getOrdersUseCase(orders, products));
        assertNotNull(config.updateOrderUseCase(orders, products, inventory,
                activityNotifications));
        assertNotNull(
                config.confirmOrderUseCase(orders, inventory, activityNotifications));
        assertNotNull(
                config.cancelOrderUseCase(orders, inventory, activityNotifications));
        assertNotNull(config.convertToInvoiceUseCase(orders, invoices,
                activityNotifications));
        assertNotNull(config.deleteOrderUseCase(orders, inventory,
                activityNotifications));
        assertNotNull(config.getProductsForOrderUseCase(customers, products));
    }
}
