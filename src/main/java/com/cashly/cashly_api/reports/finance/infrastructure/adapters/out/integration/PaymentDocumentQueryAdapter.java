package com.cashly.cashly_api.reports.finance.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.payments.application.ports.in.PaymentDocumentQuery;
import com.cashly.cashly_api.reports.finance.application.ports.out.PaymentReportQueryGateway;
import com.cashly.cashly_api.reports.finance.application.dto.PaymentReportData;
import org.springframework.stereotype.Component;
import java.util.UUID;
@Component
public class PaymentDocumentQueryAdapter implements PaymentReportQueryGateway {
    private final PaymentDocumentQuery query;
    public PaymentDocumentQueryAdapter(PaymentDocumentQuery query) { this.query = query; }
    public PaymentReportData find(UUID id) {
        var payment = query.find(id);
        return new PaymentReportData(payment.id(), payment.paymentNumber(), payment.amount(), payment.referenceNumber(), payment.notes(), payment.status(), payment.createdAt());
    }
}