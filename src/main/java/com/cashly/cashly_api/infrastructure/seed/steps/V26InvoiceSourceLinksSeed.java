package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;
import static com.cashly.cashly_api.infrastructure.seed.SeedDocuments.*;

import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.infrastructure.seed.SeedDocuments;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderEntity;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteJpaEntity;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteStatus;
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
        for (var sample : SeedDocuments.additional()) {
            var invoice = entityManager.find(InvoiceEntity.class,
                    id("invoice." + sample.key()));
            if (invoice == null
                    || !PENDING_LINKS_NOTES.equals(invoice.getNotes())) {
                continue;
            }
            var order = entityManager.find(OrderEntity.class,
                    id("order." + sample.key()));
            var quote = entityManager.find(QuoteJpaEntity.class,
                    id("quote." + sample.key()));
            boolean issued = !InvoiceStatus.DRAFT.name()
                    .equals(invoice.getStatus())
                    && !InvoiceStatus.CANCELLED.name()
                            .equals(invoice.getStatus());
            if (issued && order != null
                    && OrderStatus.INVOICED.name().equals(order.getStatus())
                    && invoice.getId().equals(order.getInvoiceId())) {
                if (!invoice.getOrderIds().contains(order.getId())) {
                    invoice.getOrderIds().add(order.getId());
                }
            } else {
                invoice.setOrderId(null);
                invoice.setOrderNumber(null);
            }
            if (issued && quote != null
                    && QuoteStatus.CONVERTED_TO_INVOICE.name()
                            .equals(quote.getStatus())
                    && invoice.getId().equals(quote.getInvoiceId())
                    && !invoice.getQuoteIds().contains(quote.getId())) {
                invoice.getQuoteIds().add(quote.getId());
            }
            invoice.setNotes(SCENARIO_NOTES);
        }
    }
    private void link(java.util.UUID invoiceId, java.util.UUID orderId,
            java.util.UUID quoteId) {
        var invoice = entityManager.find(InvoiceEntity.class, invoiceId);
        var quote = entityManager.find(QuoteJpaEntity.class, quoteId);
        if (!invoice.getOrderIds().contains(orderId))
            invoice.getOrderIds().add(orderId);
        if (!invoice.getQuoteIds().contains(quoteId))
            invoice.getQuoteIds().add(quoteId);
        if (quote != null && QuoteStatus.DRAFT.name().equals(quote.getStatus())) {
            quote.setStatus(QuoteStatus.CONVERTED_TO_INVOICE.name());
            quote.setInvoiceId(invoiceId);
            quote.setInvoiceNumber(invoice.getInvoiceNumber());
        }
    }
}
