package com.cashly.cashly_api.sales.orders.reporting.infrastructure.config;

import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.cashly.cashly_api.sales.orders.reporting.application.ports.out.OrderReportQueryGateway;
import com.cashly.cashly_api.sales.orders.reporting.application.ports.out.OrderPdfPort;
import com.cashly.cashly_api.sales.orders.reporting.application.ports.out.OrdersReportPdfPort;
import com.cashly.cashly_api.sales.orders.reporting.application.generate_order_pdf.use_case.GenerateOrderPdfUseCase;
import com.cashly.cashly_api.sales.orders.reporting.application.generate_orders_report_pdf.use_case.GenerateOrdersReportPdfUseCase;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdersReportConfig Unit Tests")
class OrdersReportConfigTest {

    @Mock
    private OrderReportQueryGateway query;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private OrderPdfPort orderPdfPort;

    @Mock
    private OrdersReportPdfPort ordersReportPdfPort;

    @Test
    @DisplayName("should create use case beans")
    void should_create_use_case_beans() {
        var config = new OrdersReportConfig();

        var orderPdfUseCase = config.generateOrderPdfUseCase(
                query, orderPdfPort);
        assertNotNull(orderPdfUseCase);

        var reportPdfUseCase = config.generateOrdersReportPdfUseCase(query, ordersReportPdfPort);
        assertNotNull(reportPdfUseCase);
    }

    @Test
    @DisplayName("should create port beans")
    void should_create_port_beans() {
        var config = new OrdersReportConfig();

        assertNotNull(config.orderPdfPort(new PdfBranding()));
        assertNotNull(config.ordersReportPdfPort(new PdfBranding()));
    }
}
