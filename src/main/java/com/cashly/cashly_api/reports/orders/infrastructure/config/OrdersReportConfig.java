package com.cashly.cashly_api.reports.orders.infrastructure.config;
import com.cashly.cashly_api.reports.orders.application.contracts.OrderReportQueryGateway;
import com.cashly.cashly_api.reports.orders.application.port.OrderPdfPort;
import com.cashly.cashly_api.reports.orders.application.port.OrdersReportPdfPort;
import com.cashly.cashly_api.reports.orders.application.usecases.GenerateOrderPdfUseCase;
import com.cashly.cashly_api.reports.orders.application.usecases.GenerateOrderPdfUseCaseImpl;
import com.cashly.cashly_api.reports.orders.application.usecases.GenerateOrdersReportPdfUseCase;
import com.cashly.cashly_api.reports.orders.application.usecases.GenerateOrdersReportPdfUseCaseImpl;
import com.cashly.cashly_api.reports.orders.infrastructure.pdf.OrderPdfGenerator;
import com.cashly.cashly_api.reports.orders.infrastructure.pdf.OrdersReportPdfGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class OrdersReportConfig {
    @Bean
    public OrderPdfPort orderPdfPort() { return new OrderPdfGenerator(); }
    @Bean
    public OrdersReportPdfPort ordersReportPdfPort() { return new OrdersReportPdfGenerator(); }
    @Bean
    public GenerateOrderPdfUseCase generateOrderPdfUseCase(OrderReportQueryGateway query, OrderPdfPort pdf) {
        return new GenerateOrderPdfUseCaseImpl(query, pdf);
    }
    @Bean
    public GenerateOrdersReportPdfUseCase generateOrdersReportPdfUseCase(OrderReportQueryGateway query, OrdersReportPdfPort pdf) {
        return new GenerateOrdersReportPdfUseCaseImpl(query, pdf);
    }
}