package com.cashly.cashly_api.reports.orders.application.usecases;

import com.cashly.cashly_api.reports.orders.application.dto.OrderReportFilter;
import com.cashly.cashly_api.reports.orders.application.ports.out.OrderReportQueryGateway;
import com.cashly.cashly_api.reports.orders.application.ports.out.OrdersReportPdfPort;



public class GenerateOrdersReportPdfUseCaseImpl implements GenerateOrdersReportPdfUseCase {

    private final OrderReportQueryGateway query;
    private final OrdersReportPdfPort ordersReportPdfPort;

    public GenerateOrdersReportPdfUseCaseImpl(OrderReportQueryGateway query, OrdersReportPdfPort ordersReportPdfPort) {
        this.query = query;
        this.ordersReportPdfPort = ordersReportPdfPort;
    }

    @Override
    public byte[] execute(OrderReportFilter filter, String mode) {
        return ordersReportPdfPort.generatePdf(query.findAll(filter), mode != null ? mode : "simple");
    }
}