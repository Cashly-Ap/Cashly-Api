package com.cashly.cashly_api.payments.collection.application.services;
import com.cashly.cashly_api.payments.collection.application.ports.in.PaymentDocumentQuery;
import com.cashly.cashly_api.payments.collection.application.ports.in.PaymentDocumentSnapshot;
import com.cashly.cashly_api.payments.collection.application.queries.GetPaymentByIdUseCase;
import java.util.UUID;
public class PaymentDocumentQueryService implements PaymentDocumentQuery {
    private final GetPaymentByIdUseCase query;
    public PaymentDocumentQueryService(GetPaymentByIdUseCase query) { this.query = query; }
    public PaymentDocumentSnapshot find(UUID id) {
        var payment = query.execute(id);
        return new PaymentDocumentSnapshot(payment.id(), payment.paymentNumber(), payment.amount(), payment.referenceNumber(), payment.notes(), payment.status(), payment.createdAt());
    }
}