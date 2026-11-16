package com.cashly.cashly_api.sales.orders.infrastructure.config;

import com.cashly.cashly_api.sales.orders.application.ports.out.InvoiceCreationGateway;
import com.cashly.cashly_api.sales.orders.application.cancel.use_case.CancelOrderUseCase;
import com.cashly.cashly_api.sales.orders.application.confirm.use_case.ConfirmOrderUseCase;
import com.cashly.cashly_api.sales.orders.application.convert_to_invoice.use_case.ConvertToInvoiceUseCase;
import com.cashly.cashly_api.sales.orders.application.create.use_case.CreateOrderUseCase;
import com.cashly.cashly_api.sales.orders.application.delete.use_case.DeleteOrderUseCase;
import com.cashly.cashly_api.sales.orders.application.get.use_case.GetOrderByIdUseCase;
import com.cashly.cashly_api.sales.orders.application.list.use_case.GetOrdersUseCase;
import com.cashly.cashly_api.sales.orders.application.products.use_case.GetProductsForOrderUseCase;
import com.cashly.cashly_api.sales.orders.application.update.use_case.UpdateOrderUseCase;
import com.cashly.cashly_api.sales.orders.domain.repository.OrderRepository;
import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;
import com.cashly.cashly_api.sales.orders.application.ports.out.StockAvailabilityGateway;
import com.cashly.cashly_api.sales.orders.application.ports.out.CustomerCreditGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfig {

    @Bean
    public CreateOrderUseCase createOrderUseCase(
            OrderRepository orderRepository,
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderCustomerGateway customers,
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderProductCatalogGateway products,
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderActorGateway actors,
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway inventory,
            ActivityNotificationRecorder activityNotifications) {
        return new CreateOrderUseCase(orderRepository, customers, products,
                actors, inventory, activityNotifications);
    }

    @Bean
    public GetOrderByIdUseCase getOrderByIdUseCase(
            OrderRepository orderRepository,
            StockAvailabilityGateway stockEnrichmentService,
            CustomerCreditGateway creditGuard) {
        return new GetOrderByIdUseCase(orderRepository, stockEnrichmentService,
                creditGuard);
    }

    @Bean
    public GetOrdersUseCase getOrdersUseCase(OrderRepository orderRepository,
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderProductCatalogGateway products) {
        return new GetOrdersUseCase(orderRepository, products);
    }

    @Bean
    public UpdateOrderUseCase updateOrderUseCase(
            OrderRepository orderRepository,
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderProductCatalogGateway products,
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway inventory,
            ActivityNotificationRecorder activityNotifications) {
        return new UpdateOrderUseCase(orderRepository, products, inventory,
                activityNotifications);
    }

    @Bean
    public ConfirmOrderUseCase confirmOrderUseCase(
            OrderRepository orderRepository,
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway inventory,
            ActivityNotificationRecorder activityNotifications) {
        return new ConfirmOrderUseCase(orderRepository, inventory,
                activityNotifications);
    }

    @Bean
    public CancelOrderUseCase cancelOrderUseCase(
            OrderRepository orderRepository,
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway inventory,
            ActivityNotificationRecorder activityNotifications) {
        return new CancelOrderUseCase(orderRepository, inventory,
                activityNotifications);
    }

    @Bean
    public ConvertToInvoiceUseCase convertToInvoiceUseCase(
            OrderRepository orderRepository,
            InvoiceCreationGateway invoiceCreationGateway,
            ActivityNotificationRecorder activityNotifications) {
        return new ConvertToInvoiceUseCase(orderRepository,
                invoiceCreationGateway, activityNotifications);
    }

    @Bean
    public DeleteOrderUseCase deleteOrderUseCase(
            OrderRepository orderRepository,
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderInventoryGateway inventory,
            ActivityNotificationRecorder activityNotifications) {
        return new DeleteOrderUseCase(orderRepository, inventory,
                activityNotifications);
    }

    @Bean
    public GetProductsForOrderUseCase getProductsForOrderUseCase(
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderCustomerGateway customers,
            com.cashly.cashly_api.sales.orders.application.ports.out.OrderProductCatalogGateway products) {
        return new GetProductsForOrderUseCase(customers, products);
    }
}
