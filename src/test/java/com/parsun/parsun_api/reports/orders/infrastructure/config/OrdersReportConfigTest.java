package com.parsun.parsun_api.reports.orders.infrastructure.config;

import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.reports.orders.application.port.OrderPdfPort;
import com.parsun.parsun_api.reports.orders.application.port.OrdersReportPdfPort;
import com.parsun.parsun_api.reports.orders.application.usecases.GenerateOrderPdfUseCase;
import com.parsun.parsun_api.reports.orders.application.usecases.GenerateOrdersReportPdfUseCase;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
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
    private OrderRepository orderRepository;

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
                orderRepository, warehouseStockRepository, warehouseRepository, orderPdfPort);
        assertNotNull(orderPdfUseCase);

        var reportPdfUseCase = config.generateOrdersReportPdfUseCase(ordersReportPdfPort);
        assertNotNull(reportPdfUseCase);
    }

    @Test
    @DisplayName("should create port beans")
    void should_create_port_beans() {
        var config = new OrdersReportConfig();

        assertNotNull(config.orderPdfPort());
        assertNotNull(config.ordersReportPdfPort());
    }
}
