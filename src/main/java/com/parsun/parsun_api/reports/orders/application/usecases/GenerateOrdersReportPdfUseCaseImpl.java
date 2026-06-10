package com.parsun.parsun_api.reports.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderListResponse;
import com.parsun.parsun_api.reports.orders.application.port.OrdersReportPdfPort;

import java.util.List;


public class GenerateOrdersReportPdfUseCaseImpl implements GenerateOrdersReportPdfUseCase {

    private final OrdersReportPdfPort ordersReportPdfPort;

    public GenerateOrdersReportPdfUseCaseImpl(OrdersReportPdfPort ordersReportPdfPort) {
        this.ordersReportPdfPort = ordersReportPdfPort;
    }

    @Override
    public byte[] execute(List<OrderListResponse> orders, String mode) {
        return ordersReportPdfPort.generatePdf(orders, mode != null ? mode : "simple");
    }
}