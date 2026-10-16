package com.cashly.cashly_api.orders.reporting.infrastructure.config;
import com.cashly.cashly_api.orders.reporting.application.ports.out.OrderReportQueryGateway;
import com.cashly.cashly_api.orders.reporting.application.ports.out.OrderPdfPort;
import com.cashly.cashly_api.orders.reporting.application.ports.out.OrdersReportPdfPort;
import com.cashly.cashly_api.orders.reporting.application.generate_order_pdf.use_case.GenerateOrderPdfUseCase;
import com.cashly.cashly_api.orders.reporting.application.generate_order_pdf.services.GenerateOrderPdfUseCaseImpl;
import com.cashly.cashly_api.orders.reporting.application.generate_orders_report_pdf.use_case.GenerateOrdersReportPdfUseCase;
import com.cashly.cashly_api.orders.reporting.application.generate_orders_report_pdf.services.GenerateOrdersReportPdfUseCaseImpl;
import com.cashly.cashly_api.orders.reporting.infrastructure.pdf.OrderPdfGenerator;
import com.cashly.cashly_api.orders.reporting.infrastructure.pdf.OrdersReportPdfGenerator;
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