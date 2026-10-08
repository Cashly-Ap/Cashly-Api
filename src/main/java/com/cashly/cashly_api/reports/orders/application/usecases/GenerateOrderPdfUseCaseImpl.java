package com.cashly.cashly_api.reports.orders.application.usecases;

import com.cashly.cashly_api.reports.orders.application.contracts.OrderReportQueryGateway;
import com.cashly.cashly_api.reports.orders.application.port.OrderPdfPort;
import java.util.UUID;

public class GenerateOrderPdfUseCaseImpl implements GenerateOrderPdfUseCase {
    private final OrderReportQueryGateway query;
    private final OrderPdfPort pdf;
    public GenerateOrderPdfUseCaseImpl(OrderReportQueryGateway query, OrderPdfPort pdf) {
        this.query = query;
        this.pdf = pdf;
    }
    public byte[] execute(UUID id) { return pdf.generatePdf(query.find(id)); }
}
