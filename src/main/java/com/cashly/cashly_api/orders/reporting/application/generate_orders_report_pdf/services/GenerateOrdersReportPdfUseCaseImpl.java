package com.cashly.cashly_api.orders.reporting.application.generate_orders_report_pdf.services;

import com.cashly.cashly_api.orders.reporting.application.generate_orders_report_pdf.use_case.GenerateOrdersReportPdfUseCase;

import com.cashly.cashly_api.orders.reporting.application.dto.model.OrderReportFilter;
import com.cashly.cashly_api.orders.reporting.application.ports.out.OrderReportQueryGateway;
import com.cashly.cashly_api.orders.reporting.application.ports.out.OrdersReportPdfPort;



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