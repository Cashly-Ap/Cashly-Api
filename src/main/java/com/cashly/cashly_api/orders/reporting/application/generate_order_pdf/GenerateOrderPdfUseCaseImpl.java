package com.cashly.cashly_api.orders.reporting.application.generate_order_pdf;

import com.cashly.cashly_api.orders.reporting.application.ports.out.OrderReportQueryGateway;
import com.cashly.cashly_api.orders.reporting.application.ports.out.OrderPdfPort;
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
