package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.infrastructure.seed.SeedDocuments;
import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditAction;
import com.cashly.cashly_api.platform.audit.domain.valueobjects.AuditEntityType;
import com.cashly.cashly_api.platform.audit.infrastructure.persistence.entities.AuditEventEntity;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseStatus;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class V30AuditSeed implements SeedStep {

    private static final String ADMIN = "Cashly Administrator";
    private static final String OPERATOR = "Sales Operator";
    private static final String COMPANY_LABEL = "Cashly Demo, C.A.";
    private static final Instant BASE = Instant.parse("2025-01-01T08:00:00Z");
    private static final long STEP_SECONDS = 1_800L;

    private final EntityManager entityManager;
    private int sequence;

    public V30AuditSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public int version() {
        return 30;
    }

    @Override
    public void seed() {
        platform();
        catalog();
        partners();
        salesDocuments();
        financeDocuments();
        procurementDocuments();
    }

    private void platform() {
        create("role-admin", "platform", AuditEntityType.ROLE,
                SYSTEM_ADMIN_ROLE, "System administrator", ADMIN_USER, ADMIN,
                "ACTIVE");
        create("role-operator", "platform", AuditEntityType.ROLE,
                OPERATOR_ROLE, "Operator", ADMIN_USER, ADMIN, "ACTIVE");
        create("department-admin", "platform", AuditEntityType.DEPARTMENT,
                ADMINISTRATION_DEPARTMENT, "Administration", ADMIN_USER, ADMIN,
                "ACTIVE");
        create("department-sales", "platform", AuditEntityType.DEPARTMENT,
                SALES_DEPARTMENT, "Sales", ADMIN_USER, ADMIN, "ACTIVE");
        create("user-admin", "platform", AuditEntityType.USER, ADMIN_USER,
                "Gabriel Administrator", ADMIN_USER, ADMIN, "ACTIVE");
        create("user-operator", "platform", AuditEntityType.USER,
                OPERATOR_USER, "Sales Operator", ADMIN_USER, ADMIN, "ACTIVE");
        create("company", "platform", AuditEntityType.COMPANY, COMPANY,
                COMPANY_LABEL, ADMIN_USER, ADMIN, "ACTIVE");
        edit("company-edit", "platform", AuditEntityType.COMPANY, COMPANY,
                COMPANY_LABEL, ADMIN_USER, ADMIN, "tradeName", "Cashly Demo",
                "Cashly Demo, C.A.");
    }

    private void catalog() {
        create("category-food", "catalog", AuditEntityType.CATEGORY,
                CATEGORY_FOOD, "Food", ADMIN_USER, ADMIN, "ACTIVE");
        create("category-drinks", "catalog", AuditEntityType.CATEGORY,
                CATEGORY_DRINKS, "Drinks", ADMIN_USER, ADMIN, "ACTIVE");
        create("price-base", "catalog", AuditEntityType.PRICE_TYPE,
                id("price-type.base"), "Base", ADMIN_USER, ADMIN, "ACTIVE");
        create("price-wholesale", "catalog", AuditEntityType.PRICE_TYPE,
                id("price-type.wholesale"), "Wholesale", ADMIN_USER, ADMIN,
                "ACTIVE");
        create("price-special", "catalog", AuditEntityType.PRICE_TYPE,
                id("price-type.special"), "Special", ADMIN_USER, ADMIN,
                "ACTIVE");
        create("product-coffee", "catalog", AuditEntityType.PRODUCT,
                PRODUCT_COFFEE, "Ground coffee", ADMIN_USER, ADMIN, "ACTIVE");
        create("product-water", "catalog", AuditEntityType.PRODUCT,
                PRODUCT_WATER, "Mineral water", ADMIN_USER, ADMIN, "ACTIVE");
        create("product-bundle", "catalog", AuditEntityType.PRODUCT,
                id("product.breakfast-bundle"), "Breakfast bundle", ADMIN_USER,
                ADMIN, "ACTIVE");
        edit("product-coffee-price", "catalog", AuditEntityType.PRODUCT,
                PRODUCT_COFFEE, "Ground coffee", ADMIN_USER, ADMIN,
                "purchasePrice", "4.00", "4.50");
        create("warehouse-main", "inventory", AuditEntityType.WAREHOUSE,
                WAREHOUSE_MAIN, "Main warehouse", ADMIN_USER, ADMIN, "ACTIVE");
        create("warehouse-reserve", "inventory", AuditEntityType.WAREHOUSE,
                WAREHOUSE_RESERVE, "Reserve warehouse", ADMIN_USER, ADMIN,
                "ACTIVE");
        action("stock-coffee-entry", "inventory", AuditEntityType.WAREHOUSE,
                WAREHOUSE_MAIN, "Main warehouse", AuditAction.RECORD_ENTRY,
                ADMIN_USER, ADMIN, Map.of("productId", PRODUCT_COFFEE.toString(),
                        "productName", "Ground coffee", "quantity", 100));
        action("stock-water-entry", "inventory", AuditEntityType.WAREHOUSE,
                WAREHOUSE_MAIN, "Main warehouse", AuditAction.RECORD_ENTRY,
                ADMIN_USER, ADMIN, Map.of("productId", PRODUCT_WATER.toString(),
                        "productName", "Mineral water", "quantity", 150));
        action("stock-coffee-transfer", "inventory",
                AuditEntityType.WAREHOUSE, WAREHOUSE_RESERVE,
                "Reserve warehouse", AuditAction.TRANSFER, ADMIN_USER, ADMIN,
                Map.of("sourceWarehouseId", WAREHOUSE_MAIN.toString(),
                        "productId", PRODUCT_COFFEE.toString(), "quantity", 40));
    }

    private void partners() {
        create("customer-one", "partners", AuditEntityType.CUSTOMER,
                CUSTOMER_ONE, "Mercado Central, C.A.", ADMIN_USER, ADMIN,
                "ACTIVE");
        create("customer-two", "partners", AuditEntityType.CUSTOMER,
                CUSTOMER_TWO, "Ana Pérez", ADMIN_USER, ADMIN, "ACTIVE");
        action("customer-one-advance", "partners", AuditEntityType.CUSTOMER,
                CUSTOMER_ONE, "Mercado Central, C.A.", AuditAction.ADVANCE,
                ADMIN_USER, ADMIN, Map.of("amount", "120.00", "method", "CASH"));
        action("customer-two-advance", "partners", AuditEntityType.CUSTOMER,
                CUSTOMER_TWO, "Ana Pérez", AuditAction.ADVANCE, ADMIN_USER,
                ADMIN, Map.of("amount", "40.00", "method", "TRANSFER"));
        action("customer-one-access", "partners", AuditEntityType.CUSTOMER,
                CUSTOMER_ONE, "Mercado Central, C.A.", AuditAction.EDIT,
                ADMIN_USER, ADMIN,
                Map.of("warehouseId", WAREHOUSE_MAIN.toString(),
                        "warehouseName", "Main warehouse", "access", "GRANTED"));
        action("customer-two-access", "partners", AuditEntityType.CUSTOMER,
                CUSTOMER_TWO, "Ana Pérez", AuditAction.EDIT, ADMIN_USER, ADMIN,
                Map.of("warehouseId", WAREHOUSE_MAIN.toString(),
                        "warehouseName", "Main warehouse", "access", "GRANTED"));
        create("supplier-one", "partners", AuditEntityType.SUPPLIER,
                id("supplier.one"), "Coffee Growers, C.A.", ADMIN_USER, ADMIN,
                "ACTIVE");
        create("supplier-two", "partners", AuditEntityType.SUPPLIER,
                id("supplier.two"), "Spring Water, C.A.", ADMIN_USER, ADMIN,
                "ACTIVE");
    }

    private void salesDocuments() {
        create("quote-one", "sales", AuditEntityType.QUOTE, QUOTE_ONE,
                "PRE-2025-0001", OPERATOR_USER, OPERATOR, "DRAFT");
        create("quote-two", "sales", AuditEntityType.QUOTE, QUOTE_TWO,
                "PRE-2025-0002", OPERATOR_USER, OPERATOR, "DRAFT");
        create("order-one", "sales", AuditEntityType.ORDER, ORDER_ONE,
                "ORD-2025-0001", OPERATOR_USER, OPERATOR, "CONFIRMED");
        create("order-two", "sales", AuditEntityType.ORDER, ORDER_TWO,
                "ORD-2025-0002", OPERATOR_USER, OPERATOR, "CONFIRMED");
        for (var sample : SeedDocuments.additional()) {
            salesSample(sample);
        }
    }

    private void salesSample(SeedDocuments.Sample sample) {
        var scenario = sample.scenario();
        String quoteId = id("quote." + sample.key()).toString();
        String quoteNumber = sample.documentNumber("PRE");
        create("quote-create-" + sample.key(), "sales", AuditEntityType.QUOTE,
                id("quote." + sample.key()), quoteNumber, OPERATOR_USER,
                OPERATOR, scenario.quote().name());
        if (scenario.quote() == QuoteStatus.CANCELLED) {
            cancel("quote-cancel-" + sample.key(), "sales",
                    AuditEntityType.QUOTE, id("quote." + sample.key()),
                    quoteNumber, OPERATOR_USER, OPERATOR,
                    scenario.quote().name(), "Cancelled demonstration quote");
        } else if (scenario.quote() == QuoteStatus.CONVERTED_TO_ORDER) {
            action("quote-order-" + sample.key(), "sales",
                    AuditEntityType.QUOTE, id("quote." + sample.key()),
                    quoteNumber, AuditAction.CONVERT_TO_ORDER, OPERATOR_USER,
                    OPERATOR, Map.of("sourceId", quoteId, "sourceNumber",
                            quoteNumber, "targetId",
                            id("order." + sample.key()).toString(),
                            "targetNumber", sample.documentNumber("ORD")));
        } else if (scenario.quote() == QuoteStatus.CONVERTED_TO_INVOICE) {
            action("quote-invoice-" + sample.key(), "sales",
                    AuditEntityType.QUOTE, id("quote." + sample.key()),
                    quoteNumber, AuditAction.CONVERT_TO_INVOICE, OPERATOR_USER,
                    OPERATOR, Map.of("sourceId", quoteId, "sourceNumber",
                            quoteNumber, "targetId",
                            id("invoice." + sample.key()).toString(),
                            "targetNumber", sample.documentNumber("INV")));
        }

        String orderNumber = sample.documentNumber("ORD");
        create("order-create-" + sample.key(), "sales", AuditEntityType.ORDER,
                id("order." + sample.key()), orderNumber, OPERATOR_USER,
                OPERATOR, scenario.order().name());
        if (scenario.order() == OrderStatus.CANCELLED) {
            cancel("order-cancel-" + sample.key(), "sales",
                    AuditEntityType.ORDER, id("order." + sample.key()),
                    orderNumber, OPERATOR_USER, OPERATOR,
                    scenario.order().name(), "Cancelled demonstration order");
        } else if (scenario.order() != OrderStatus.DRAFT) {
            action("order-confirm-" + sample.key(), "sales",
                    AuditEntityType.ORDER, id("order." + sample.key()),
                    orderNumber, AuditAction.CONFIRM, OPERATOR_USER, OPERATOR,
                    Map.of("statusFrom", "DRAFT", "statusTo",
                            scenario.order().name(), "documentNumber",
                            orderNumber));
        }

        String invoiceNumber = sample.documentNumber("INV");
        create("invoice-create-" + sample.key(), "finance",
                AuditEntityType.INVOICE, id("invoice." + sample.key()),
                invoiceNumber, ADMIN_USER, ADMIN, scenario.invoice().name());
        if (scenario.invoice() == InvoiceStatus.CANCELLED) {
            cancel("invoice-cancel-" + sample.key(), "finance",
                    AuditEntityType.INVOICE, id("invoice." + sample.key()),
                    invoiceNumber, ADMIN_USER, ADMIN,
                    scenario.invoice().name(), "Cancelled demonstration invoice");
        } else if (scenario.invoice() != InvoiceStatus.DRAFT) {
            action("invoice-confirm-" + sample.key(), "finance",
                    AuditEntityType.INVOICE, id("invoice." + sample.key()),
                    invoiceNumber, AuditAction.CONFIRM, ADMIN_USER, ADMIN,
                    Map.of("statusFrom", "DRAFT", "statusTo",
                            scenario.invoice().name(), "documentNumber",
                            invoiceNumber));
        }
        if (scenario.invoice() == InvoiceStatus.PARTIALLY_PAID) {
            action("invoice-partial-" + sample.key(), "finance",
                    AuditEntityType.INVOICE, id("invoice." + sample.key()),
                    invoiceNumber, AuditAction.PARTIAL_PAYMENT, OPERATOR_USER,
                    OPERATOR, Map.of("amount", "partial", "method",
                            scenario.paymentType().name()));
        } else if (scenario.invoice() == InvoiceStatus.PAID) {
            action("invoice-pay-" + sample.key(), "finance",
                    AuditEntityType.INVOICE, id("invoice." + sample.key()),
                    invoiceNumber, AuditAction.PAY, OPERATOR_USER, OPERATOR,
                    Map.of("amount", "full", "method",
                            scenario.paymentType().name()));
        }
    }

    private void financeDocuments() {
        create("invoice-one", "finance", AuditEntityType.INVOICE, INVOICE_ONE,
                "INV-2025-0001", ADMIN_USER, ADMIN, "PENDING_PAYMENT");
        action("invoice-one-confirm", "finance", AuditEntityType.INVOICE,
                INVOICE_ONE, "INV-2025-0001", AuditAction.CONFIRM, ADMIN_USER,
                ADMIN, Map.of("statusFrom", "DRAFT", "statusTo",
                        "PENDING_PAYMENT", "documentNumber", "INV-2025-0001"));
        action("invoice-one-pay", "finance", AuditEntityType.INVOICE,
                INVOICE_ONE, "INV-2025-0001", AuditAction.PAY, OPERATOR_USER,
                OPERATOR, Map.of("amount", "10.00", "method", "CASH"));
        create("invoice-two", "finance", AuditEntityType.INVOICE, INVOICE_TWO,
                "INV-2025-0002", ADMIN_USER, ADMIN, "PENDING_PAYMENT");
        action("invoice-two-confirm", "finance", AuditEntityType.INVOICE,
                INVOICE_TWO, "INV-2025-0002", AuditAction.CONFIRM, ADMIN_USER,
                ADMIN, Map.of("statusFrom", "DRAFT", "statusTo",
                        "PENDING_PAYMENT", "documentNumber", "INV-2025-0002"));
        action("invoice-two-partial", "finance", AuditEntityType.INVOICE,
                INVOICE_TWO, "INV-2025-0002", AuditAction.PARTIAL_PAYMENT,
                OPERATOR_USER, OPERATOR, Map.of("amount", "3.00", "method",
                        "TRANSFER"));
        create("payment-one", "finance", AuditEntityType.PAYMENT, PAYMENT_ONE,
                "PAY-2025-0001", ADMIN_USER, ADMIN, "APPLIED");
        action("payment-one-approve", "finance", AuditEntityType.PAYMENT,
                PAYMENT_ONE, "PAY-2025-0001", AuditAction.APPROVE, ADMIN_USER,
                ADMIN, Map.of("previousStatus", "PENDING", "newStatus",
                        "APPLIED", "amount", "10.00"));
        create("payment-two", "finance", AuditEntityType.PAYMENT, PAYMENT_TWO,
                "PAY-2025-0002", ADMIN_USER, ADMIN, "APPLIED");
        action("payment-two-approve", "finance", AuditEntityType.PAYMENT,
                PAYMENT_TWO, "PAY-2025-0002", AuditAction.APPROVE, ADMIN_USER,
                ADMIN, Map.of("previousStatus", "PENDING", "newStatus",
                        "APPLIED", "amount", "3.00"));
        for (var sample : SeedDocuments.additional()) {
            paymentSample(sample);
        }
        create("return-one", "finance", AuditEntityType.RETURN, RETURN_ONE,
                "RET-2025-0001", ADMIN_USER, ADMIN, "COMPLETED");
        create("return-two", "finance", AuditEntityType.RETURN, RETURN_TWO,
                "RET-2025-0002", ADMIN_USER, ADMIN, "COMPLETED");
        create("credit-note-one", "finance", AuditEntityType.CREDIT_NOTE,
                id("credit-note.one"), "CN-2025-0001", ADMIN_USER, ADMIN,
                "ISSUED");
        create("credit-note-two", "finance", AuditEntityType.CREDIT_NOTE,
                id("credit-note.two"), "CN-2025-0002", ADMIN_USER, ADMIN,
                "ISSUED");
        create("debit-note-one", "finance", AuditEntityType.DEBIT_NOTE,
                id("debit-note.one"), "DN-2025-0001", ADMIN_USER, ADMIN,
                "ISSUED");
        create("debit-note-two", "finance", AuditEntityType.DEBIT_NOTE,
                id("debit-note.two"), "DN-2025-0002", ADMIN_USER, ADMIN,
                "ISSUED");
        action("register-closed-open", "finance",
                AuditEntityType.CASH_REGISTER, id("cash-register.closed"),
                "Cash register 2025-01-10", AuditAction.OPEN, ADMIN_USER,
                ADMIN, Map.of("openingBalance", "100.00"));
        action("register-closed-close", "finance",
                AuditEntityType.CASH_REGISTER, id("cash-register.closed"),
                "Cash register 2025-01-10", AuditAction.CLOSE, ADMIN_USER,
                ADMIN, Map.of("declaredBalance", "113.00", "difference",
                        "0.00"));
        action("register-open", "finance", AuditEntityType.CASH_REGISTER,
                id("cash-register.open"), "Cash register 2025-01-13",
                AuditAction.OPEN, ADMIN_USER, ADMIN,
                Map.of("openingBalance", "100.00"));
        action("cash-movement-one", "finance",
                AuditEntityType.CASH_MOVEMENT, id("cash-movement.one"),
                "Cash movement PAY-2025-0001", AuditAction.RECORD_MOVEMENT,
                ADMIN_USER, ADMIN, Map.of("direction", "IN", "type", "PAYMENT",
                        "amount", "10.00"));
        action("cash-movement-two", "finance",
                AuditEntityType.CASH_MOVEMENT, id("cash-movement.two"),
                "Petty cash expense", AuditAction.RECORD_MOVEMENT, ADMIN_USER,
                ADMIN, Map.of("direction", "OUT", "type", "EXPENSE", "amount",
                        "2.00"));
        create("payment-method-cash", "finance",
                AuditEntityType.PAYMENT_METHOD, id("payment-method.cash"),
                "Cash", ADMIN_USER, ADMIN, "ACTIVE");
        create("payment-method-transfer", "finance",
                AuditEntityType.PAYMENT_METHOD, id("payment-method.transfer"),
                "Bank transfer", ADMIN_USER, ADMIN, "ACTIVE");
    }

    private void paymentSample(SeedDocuments.Sample sample) {
        var invoice = sample.scenario().invoice();
        if (invoice != InvoiceStatus.PAID
                && invoice != InvoiceStatus.PARTIALLY_PAID) {
            return;
        }
        UUID paymentId = id("scenario-payment." + sample.key());
        String paymentNumber = sample.documentNumber("SEEDPAY");
        create("sample-payment-create-" + sample.key(), "finance",
                AuditEntityType.PAYMENT, paymentId, paymentNumber, ADMIN_USER,
                ADMIN, "APPLIED");
        action("sample-payment-approve-" + sample.key(), "finance",
                AuditEntityType.PAYMENT, paymentId, paymentNumber,
                AuditAction.APPROVE, ADMIN_USER, ADMIN,
                Map.of("previousStatus", "PENDING", "newStatus", "APPLIED",
                        "invoiceId", id("invoice." + sample.key()).toString()));
    }

    private void procurementDocuments() {
        create("purchase-one", "procurement", AuditEntityType.PURCHASE,
                id("purchase.one"), "PUR-2025-0001", ADMIN_USER, ADMIN,
                "CONFIRMED");
        action("purchase-one-confirm", "procurement",
                AuditEntityType.PURCHASE, id("purchase.one"), "PUR-2025-0001",
                AuditAction.CONFIRM, ADMIN_USER, ADMIN,
                Map.of("statusFrom", "DRAFT", "statusTo", "CONFIRMED"));
        create("purchase-two", "procurement", AuditEntityType.PURCHASE,
                id("purchase.two"), "PUR-2025-0002", ADMIN_USER, ADMIN,
                "CONFIRMED");
        action("purchase-two-confirm", "procurement",
                AuditEntityType.PURCHASE, id("purchase.two"), "PUR-2025-0002",
                AuditAction.CONFIRM, ADMIN_USER, ADMIN,
                Map.of("statusFrom", "DRAFT", "statusTo", "CONFIRMED"));
        for (var sample : SeedDocuments.additional()) {
            purchaseSample(sample);
        }
    }

    private void purchaseSample(SeedDocuments.Sample sample) {
        var scenario = sample.scenario();
        UUID purchaseId = id("purchase." + sample.key());
        String purchaseNumber = sample.documentNumber("PUR");
        create("purchase-create-" + sample.key(), "procurement",
                AuditEntityType.PURCHASE, purchaseId, purchaseNumber, ADMIN_USER,
                ADMIN, scenario.purchase().name());
        if (scenario.purchase() == PurchaseStatus.CANCELLED) {
            cancel("purchase-cancel-" + sample.key(), "procurement",
                    AuditEntityType.PURCHASE, purchaseId, purchaseNumber,
                    ADMIN_USER, ADMIN, scenario.purchase().name(),
                    "Cancelled demonstration purchase");
        } else if (scenario.purchase() == PurchaseStatus.CONFIRMED) {
            action("purchase-confirm-" + sample.key(), "procurement",
                    AuditEntityType.PURCHASE, purchaseId, purchaseNumber,
                    AuditAction.CONFIRM, ADMIN_USER, ADMIN,
                    Map.of("statusFrom", "DRAFT", "statusTo", "CONFIRMED"));
        }
    }

    private void create(String key, String module, AuditEntityType type,
            UUID entityId, String label, UUID actorId, String actorName,
            String status) {
        action(key, module, type, entityId, label, AuditAction.CREATE, actorId,
                actorName, Map.of("status", status));
    }

    private void edit(String key, String module, AuditEntityType type,
            UUID entityId, String label, UUID actorId, String actorName,
            String field, String oldValue, String newValue) {
        action(key, module, type, entityId, label, AuditAction.EDIT, actorId,
                actorName, Map.of("field", field, "oldValue", oldValue,
                        "newValue", newValue));
    }

    private void cancel(String key, String module, AuditEntityType type,
            UUID entityId, String label, UUID actorId, String actorName,
            String statusFrom, String reason) {
        action(key, module, type, entityId, label, AuditAction.CANCEL, actorId,
                actorName, Map.of("reason", reason, "statusFrom", statusFrom,
                        "statusTo", "CANCELLED"));
    }

    private void action(String key, String module, AuditEntityType type,
            UUID entityId, String label, AuditAction action, UUID actorId,
            String actorName, Map<String, Object> metadata) {
        event(key, module, type, entityId, label, action, actorId, actorName,
                nextAt(), List.of(label), metadata);
    }

    private void event(String key, String module, AuditEntityType entityType,
            UUID entityId, String entityLabel, AuditAction action, UUID actorId,
            String actorName, Instant occurredAt, List<String> descriptionArgs,
            Map<String, Object> metadata) {
        UUID id = id("audit-event." + key);
        if (entityManager.find(AuditEventEntity.class, id) != null) {
            return;
        }
        AuditEventEntity event = new AuditEventEntity();
        event.setId(id);
        event.setCompanyId(COMPANY);
        event.setModule(module);
        event.setEntityType(entityType.name());
        event.setEntityId(entityId);
        event.setEntityLabel(entityLabel);
        event.setAction(action.name());
        event.setActorId(actorId);
        event.setActorName(actorName);
        event.setOccurredAt(occurredAt);
        event.setDescriptionKey(action.descriptionKey());
        event.setDescriptionArgs(descriptionArgs);
        event.setMetadata(metadata);
        event.setSource("WEB");
        entityManager.persist(event);
    }

    private Instant nextAt() {
        return BASE.plusSeconds((long) sequence++ * STEP_SECONDS);
    }
}
