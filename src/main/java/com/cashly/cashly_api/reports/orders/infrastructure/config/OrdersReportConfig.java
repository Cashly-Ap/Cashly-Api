package com.cashly.cashly_api.reports.orders.infrastructure.config;

import com.cashly.cashly_api.reports.orders.application.port.OrderPdfPort;
import com.cashly.cashly_api.reports.orders.application.port.OrdersReportPdfPort;
import com.cashly.cashly_api.reports.orders.application.usecases.GenerateOrderPdfUseCase;
import com.cashly.cashly_api.reports.orders.application.usecases.GenerateOrderPdfUseCaseImpl;
import com.cashly.cashly_api.reports.orders.application.usecases.GenerateOrdersReportPdfUseCase;
import com.cashly.cashly_api.reports.orders.application.usecases.GenerateOrdersReportPdfUseCaseImpl;
import com.cashly.cashly_api.reports.orders.infrastructure.pdf.OrderPdfGenerator;
import com.cashly.cashly_api.reports.orders.infrastructure.pdf.OrdersReportPdfGenerator;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.orders.application.usecases.GetOrdersUseCase;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrdersReportConfig {

    @Bean
    public OrderPdfPort orderPdfPort() {
        return new OrderPdfGenerator();
    }

    @Bean
    public OrdersReportPdfPort ordersReportPdfPort() {
        return new OrdersReportPdfGenerator();
    }

    @Bean
    public GenerateOrderPdfUseCase generateOrderPdfUseCase(
            OrderRepository orderRepository,
            WarehouseStockRepository warehouseStockRepository,
            WarehouseRepository warehouseRepository,
            OrderPdfPort orderPdfPort) {
        return new GenerateOrderPdfUseCaseImpl(
                orderRepository,
                warehouseStockRepository,
                warehouseRepository,
                orderPdfPort
        );
    }

    @Bean
    public GenerateOrdersReportPdfUseCase generateOrdersReportPdfUseCase(
            OrdersReportPdfPort ordersReportPdfPort) {
        return new GenerateOrdersReportPdfUseCaseImpl(ordersReportPdfPort);
    }
}