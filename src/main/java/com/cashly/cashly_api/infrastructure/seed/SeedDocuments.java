package com.cashly.cashly_api.infrastructure.seed;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseStatus;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.IntStream;

public final class SeedDocuments {
    public static final int YEAR = 2025;
    public static final int DOCUMENT_COUNT = 30;
    public static final int NEXT_NUMBER = DOCUMENT_COUNT + 1;
    private static final int FIRST_ADDITIONAL_NUMBER = 3;
    private static final int PRODUCT_VARIANTS = 2;
    private static final int QUANTITY_VARIANTS = 5;

    public static final String PENDING_LINKS_NOTES = "Deterministic document scenario pending links";
    public static final int MONEY_SCALE = 2;
    public static final BigDecimal PARTIAL_PAYMENT_DIVISOR = BigDecimal.TWO;
    public static final String SCENARIO_NOTES = "Deterministic document scenario";
    public static final String CANCELLATION_REASON = "Cancelled demonstration document";
    public static final LocalDateTime EVENT_AT = LocalDateTime.of(2025, 1, 15,
            10, 0);
    private static final Scenario[] SCENARIOS = {
            new Scenario(OrderStatus.DRAFT, QuoteStatus.DRAFT,
                    InvoiceStatus.DRAFT, PaymentType.CREDIT,
                    PurchaseStatus.DRAFT),
            new Scenario(OrderStatus.CONFIRMED, QuoteStatus.CONVERTED_TO_ORDER,
                    InvoiceStatus.PENDING_PAYMENT, PaymentType.CREDIT,
                    PurchaseStatus.CONFIRMED),
            new Scenario(OrderStatus.INVOICED, QuoteStatus.CONVERTED_TO_INVOICE,
                    InvoiceStatus.PARTIALLY_PAID, PaymentType.CREDIT,
                    PurchaseStatus.CANCELLED),
            new Scenario(OrderStatus.INVOICED, QuoteStatus.CONVERTED_TO_INVOICE,
                    InvoiceStatus.PAID, PaymentType.CASH,
                    PurchaseStatus.CONFIRMED),
            new Scenario(OrderStatus.INVOICED, QuoteStatus.CONVERTED_TO_ORDER,
                    InvoiceStatus.PAID, PaymentType.CREDIT,
                    PurchaseStatus.DRAFT),
            new Scenario(OrderStatus.CANCELLED, QuoteStatus.CANCELLED,
                    InvoiceStatus.CANCELLED, PaymentType.CREDIT,
                    PurchaseStatus.CANCELLED),
            new Scenario(OrderStatus.CONFIRMED, QuoteStatus.DRAFT,
                    InvoiceStatus.PENDING_PAYMENT, PaymentType.CREDIT,
                    PurchaseStatus.CONFIRMED),
            new Scenario(OrderStatus.DRAFT, QuoteStatus.DRAFT,
                    InvoiceStatus.DRAFT, PaymentType.CASH,
                    PurchaseStatus.DRAFT)};

    private SeedDocuments() {
    }

    public static List<Sample> additional() {
        return IntStream.rangeClosed(FIRST_ADDITIONAL_NUMBER, DOCUMENT_COUNT)
                .mapToObj(number -> new Sample(number,
                        number % PRODUCT_VARIANTS == 0,
                        Integer.toString(number % QUANTITY_VARIANTS + 1)))
                .toList();
    }

    public record Scenario(OrderStatus order, QuoteStatus quote,
            InvoiceStatus invoice, PaymentType paymentType,
            PurchaseStatus purchase) {
    }

    public record Sample(int number, boolean coffee, String quantity) {
        public Scenario scenario() {
            return SCENARIOS[number % SCENARIOS.length];
        }

        public String key() {
            return "sample-" + number;
        }

        public String documentNumber(String prefix) {
            return String.format(Locale.ROOT, "%s-%d-%04d", prefix, YEAR,
                    number);
        }

        public UUID customerId() {
            return coffee ? CUSTOMER_ONE : CUSTOMER_TWO;
        }

        public String customerName() {
            return coffee ? "Mercado Central, C.A." : "Ana Pérez";
        }

        public String customerRif() {
            return coffee ? "J-10000001-1" : "V-10000002-2";
        }

        public UUID productId() {
            return coffee ? PRODUCT_COFFEE : PRODUCT_WATER;
        }

        public String productName() {
            return coffee ? "Ground coffee" : "Mineral water";
        }

        public String productCode() {
            return coffee ? "PRD-0001" : "PRD-0002";
        }

        public String price() {
            return coffee ? "6.50" : "1.00";
        }

        public String cost() {
            return coffee ? "3.50" : "0.45";
        }

        public UUID supplierId() {
            return id(coffee ? "supplier.one" : "supplier.two");
        }

        public String supplierName() {
            return coffee ? "Coffee Growers, C.A." : "Spring Water, C.A.";
        }

        public String supplierRif() {
            return coffee ? "J-20000001-1" : "J-20000002-2";
        }
    }
}
