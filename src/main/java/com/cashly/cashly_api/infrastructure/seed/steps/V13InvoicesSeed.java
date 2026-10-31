package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceEntity;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceLineEntity;
import com.cashly.cashly_api.finance.billing.invoices.infrastructure.persistence.entities.InvoiceNumberSequenceEntity;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V13InvoicesSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1, 8,
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
        invoice(INVOICE_ONE, "INV-2025-0001", CUSTOMER_ONE,
                "Mercado Central, C.A.", "J-10000001-1", ORDER_ONE,
                "ORD-2025-0001", "invoice-line.one", PRODUCT_COFFEE,
                "Ground coffee", "PRD-0001", "2", "6.50");
        invoice(INVOICE_TWO, "INV-2025-0002", CUSTOMER_TWO, "Ana Pérez",
                "V-10000002-2", ORDER_TWO, "ORD-2025-0002", "invoice-line.two",
                PRODUCT_WATER, "Mineral water", "PRD-0002", "3", "1.00");
        if (entityManager.find(InvoiceNumberSequenceEntity.class,
                2025) == null) {
            var sequence = new InvoiceNumberSequenceEntity();
            sequence.setYear(2025);
            sequence.setNextValue(3);
            entityManager.persist(sequence);
        }
    }
    private void invoice(UUID id, String number, UUID customerId,
            String customerName, String rif, UUID orderId, String orderNumber,
            String lineKey, UUID productId, String productName, String code,
            String quantity, String price) {
        if (entityManager.find(InvoiceEntity.class, id) != null)
            return;
        BigDecimal qty = new BigDecimal(quantity),
                unitPrice = new BigDecimal(price),
                total = qty.multiply(unitPrice);
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
        invoice.setCreatedAt(CREATED_AT);
        invoice.setUpdatedAt(CREATED_AT);
        invoice.setIssueDate(LocalDate.of(2025, 1, 8));
        invoice.setDueDate(LocalDate.of(2025, 2, 7));
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
        invoice.getLines().add(line);
        entityManager.persist(invoice);
    }
}
