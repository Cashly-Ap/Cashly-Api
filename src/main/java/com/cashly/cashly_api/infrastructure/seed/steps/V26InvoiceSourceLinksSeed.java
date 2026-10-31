package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

@Component
public class V26InvoiceSourceLinksSeed implements SeedStep {
    private final EntityManager entityManager;
    public V26InvoiceSourceLinksSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 26;
    }
    @Override
    public void seed() {
        link(INVOICE_ONE, ORDER_ONE, QUOTE_ONE);
        link(INVOICE_TWO, ORDER_TWO, QUOTE_TWO);
    }
    private void link(java.util.UUID invoiceId, java.util.UUID orderId,
            java.util.UUID quoteId) {
        var invoice = entityManager.find(InvoiceEntity.class, invoiceId);
        if (!invoice.getOrderIds().contains(orderId))
            invoice.getOrderIds().add(orderId);
        if (!invoice.getQuoteIds().contains(quoteId))
            invoice.getQuoteIds().add(quoteId);
    }
}
