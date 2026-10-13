package com.cashly.cashly_api.payments.application.ports.in;
import java.util.List;
import java.util.UUID;
public interface CustomerCreditQuery {
    CreditStatusSnapshot getCreditStatus(UUID customerId);
    List<OverdueInvoiceSnapshot> findAllOverdueInvoices();
}
