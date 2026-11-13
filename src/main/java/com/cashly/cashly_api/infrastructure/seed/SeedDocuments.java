package com.cashly.cashly_api.infrastructure.seed;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseStatus;
import com.cashly.cashly_api.sales.orders.domain.valueobjects.OrderStatus;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

public final class SeedDocuments {
    private static final int FIRST_MONTH_INDEX = 0;
    private static final int MONTH_COUNT = 12;
    private static final YearMonth START_MONTH = YearMonth.of(2025, 12);
    private static final List<YearMonth> MONTHS = IntStream
            .range(FIRST_MONTH_INDEX, MONTH_COUNT)
            .mapToObj(START_MONTH::plusMonths)
            .toList();
    private static final int DOCUMENTS_PER_MONTH = 10;
    public static final int DOCUMENT_COUNT = MONTH_COUNT
            * DOCUMENTS_PER_MONTH;
    private static final int QUANTITY_VARIANTS = 5;
    private static final int EXPLICIT_DOCUMENTS_2026 = 2;

    public static final String PENDING_LINKS_NOTES = "Deterministic document scenario pending links";
    public static final int MONEY_SCALE = 2;
    public static final BigDecimal PARTIAL_PAYMENT_DIVISOR = BigDecimal.TWO;
    public static final String SCENARIO_NOTES = "Deterministic document scenario";
    public static final String CANCELLATION_REASON = "Cancelled demonstration document";
    public static final LocalDateTime EVENT_AT = LocalDateTime.of(2026, 1, 15,
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

    private static final List<Sample> SAMPLES = buildSamples();

    private SeedDocuments() {
    }

    public static List<Sample> additional() {
        return SAMPLES;
    }

    public static List<Integer> years() {
        return List.of(2025, 2026);
    }

    public static int nextNumberFor(int year) {
        int max = year == 2026 ? EXPLICIT_DOCUMENTS_2026 : 0;
        for (Sample sample : SAMPLES) {
            if (sample.year() == year) {
                max = Math.max(max, sample.sequence());
            }
        }
        return max + 1;
    }

    private static List<Sample> buildSamples() {
        Map<Integer, Integer> counters = new HashMap<>();
        counters.put(2026, EXPLICIT_DOCUMENTS_2026);
        List<Sample> samples = new ArrayList<>();
        int number = 1;
        for (int monthIndex = 0; monthIndex < MONTH_COUNT; monthIndex++) {
            YearMonth month = MONTHS.get(monthIndex);
            int year = month.getYear();
            for (int position = 0; position < DOCUMENTS_PER_MONTH; position++) {
                int sequence = counters.merge(year, 1, Integer::sum);
                int day = Math.min(month.lengthOfMonth(), 3 + position * 2);
                LocalDate date = month.atDay(day);
                boolean coffee = position % 2 == 0;
                String quantity = Integer.toString(
                        position % QUANTITY_VARIANTS + 1);
                samples.add(new Sample(number, year, sequence, date, coffee,
                        quantity));
                number++;
            }
        }
        return List.copyOf(samples);
    }

    public record Scenario(OrderStatus order, QuoteStatus quote,
            InvoiceStatus invoice, PaymentType paymentType,
            PurchaseStatus purchase) {
    }

    public record Sample(int number, int year, int sequence, LocalDate date,
            boolean coffee, String quantity) {
        public Scenario scenario() {
            return SCENARIOS[number % SCENARIOS.length];
        }

        public String key() {
            return "sample-" + number;
        }

        public LocalDateTime createdAt(int hour, int minute) {
            return date.atTime(hour, minute);
        }

        public String documentNumber(String prefix) {
            return String.format(Locale.ROOT, "%s-%d-%04d", prefix, year,
                    sequence);
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
