package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;
import static com.cashly.cashly_api.infrastructure.seed.SeedDocuments.*;

import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceLineEntity;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceNumberSequenceEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.infrastructure.seed.SeedDocuments;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V13InvoicesSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 8,
            8, 0);
    private final EntityManager entityManager;
    public V13InvoicesSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 13;
    }
    @Override
    public void seed() {
        invoice(INVOICE_ONE, "INV-2026-0001", CUSTOMER_ONE,
                "Mercado Central, C.A.", "J-10000001-1", ORDER_ONE,
                "ORD-2026-0001", "invoice-line.one", PRODUCT_COFFEE,
                "Ground coffee", "PRD-0001", "2", "6.50",
                LocalDate.of(2026, 1, 8), CREATED_AT);
        invoice(INVOICE_TWO, "INV-2026-0002", CUSTOMER_TWO, "Ana Pérez",
                "V-10000002-2", ORDER_TWO, "ORD-2026-0002",
                "invoice-line.two", PRODUCT_WATER, "Mineral water", "PRD-0002",
                "3", "1.00", LocalDate.of(2026, 1, 8), CREATED_AT);
        for (var sample : SeedDocuments.additional()) {
            invoice(id("invoice." + sample.key()), sample.documentNumber("INV"),
                    sample.customerId(), sample.customerName(),
                    sample.customerRif(), id("order." + sample.key()),
                    sample.documentNumber("ORD"),
                    "invoice-line." + sample.key(), sample.productId(),
                    sample.productName(), sample.productCode(),
                    sample.quantity(), sample.price(), sample.date(),
                    sample.createdAt(9, 0));
            applyScenario(sample, sample.scenario());
        }
        for (int year : SeedDocuments.years()) {
            var sequence = entityManager.find(InvoiceNumberSequenceEntity.class,
                    year);
            int nextValue = SeedDocuments.nextNumberFor(year);
            if (sequence == null) {
                sequence = new InvoiceNumberSequenceEntity();
                sequence.setYear(year);
                sequence.setNextValue(nextValue);
                entityManager.persist(sequence);
            } else if (sequence.getNextValue() < nextValue) {
                sequence.setNextValue(nextValue);
            }
        }
    }
    private void invoice(UUID id, String number, UUID customerId,
            String customerName, String rif, UUID orderId, String orderNumber,
            String lineKey, UUID productId, String productName, String code,
            String quantity, String price, LocalDate issueDate,
            LocalDateTime createdAt) {
        BigDecimal qty = new BigDecimal(quantity),
                unitPrice = new BigDecimal(price),
                total = qty.multiply(unitPrice);
        var existing = entityManager.find(InvoiceEntity.class, id);
        if (existing != null) {
            ensureLine(existing, lineKey, productId, productName, code, qty,
                    unitPrice, total);
            return;
        }
        var invoice = new InvoiceEntity();
        invoice.setId(id);
        invoice.setInvoiceNumber(number);
        invoice.setCustomerId(customerId);
        invoice.setCustomerName(customerName);
        invoice.setCustomerRif(rif);
        invoice.setCreatedBy(ADMIN_USER);
        invoice.setCreatedByName("Cashly Administrator");
        invoice.setOrderId(orderId);
        invoice.setOrderNumber(orderNumber);
        invoice.setStatus("PENDING_PAYMENT");
        invoice.setPaymentType("CREDIT");
        invoice.setNotes("Deterministic seed invoice");
        invoice.setSubtotal(total);
        invoice.setReturnedTotal(BigDecimal.ZERO);
        invoice.setCreatedAt(createdAt);
        invoice.setUpdatedAt(createdAt);
        invoice.setIssueDate(issueDate);
        invoice.setDueDate(issueDate.plusDays(30));
        invoice.getLines().add(buildLine(invoice, lineKey, productId,
                productName, code, qty, unitPrice, total));
        entityManager.persist(invoice);
    }

    private void ensureLine(InvoiceEntity invoice, String lineKey,
            UUID productId, String productName, String code, BigDecimal qty,
            BigDecimal unitPrice, BigDecimal total) {
        if (entityManager.find(InvoiceLineEntity.class, id(lineKey)) != null)
            return;
        invoice.getLines().clear();
        invoice.getLines().add(buildLine(invoice, lineKey, productId,
                productName, code, qty, unitPrice, total));
        invoice.setSubtotal(total);
        invoice.setUpdatedAt(EVENT_AT);
    }

    private InvoiceLineEntity buildLine(InvoiceEntity invoice, String lineKey,
            UUID productId, String productName, String code, BigDecimal qty,
            BigDecimal unitPrice, BigDecimal total) {
        var line = new InvoiceLineEntity();
        line.setId(id(lineKey));
        line.setInvoice(invoice);
        line.setProductId(productId);
        line.setProductName(productName);
        line.setProductCode(code);
        line.setQuantity(qty);
        line.setUnitPrice(unitPrice);
        line.setLineTotal(total);
        line.setReturnedQuantity(BigDecimal.ZERO);
        return line;
    }

    private void applyScenario(SeedDocuments.Sample sample, Scenario scenario) {
        var invoice = entityManager.find(InvoiceEntity.class,
                id("invoice." + sample.key()));
        if (invoice == null
                || !"Deterministic seed invoice".equals(invoice.getNotes())) {
            return;
        }
        invoice.setStatus(scenario.invoice().name());
        invoice.setPaymentType(scenario.paymentType().name());
        invoice.getOrderIds().clear();
        invoice.getQuoteIds().clear();
        invoice.setOrderId(null);
        invoice.setOrderNumber(null);
        if (scenario.order() == OrderStatus.INVOICED) {
            invoice.setOrderId(id("order." + sample.key()));
            invoice.setOrderNumber(sample.documentNumber("ORD"));
        }
        invoice.setCancelledAt(scenario.invoice() == InvoiceStatus.CANCELLED
                ? sample.createdAt(10, 0)
                : null);
        invoice.setCancelledReason(scenario.invoice() == InvoiceStatus.CANCELLED
                ? CANCELLATION_REASON
                : null);
        invoice.setNotes(PENDING_LINKS_NOTES);
        invoice.setUpdatedAt(sample.createdAt(10, 0));
    }

}
