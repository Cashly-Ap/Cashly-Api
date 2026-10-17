package com.cashly.cashly_api.shared.application.architecture;

import org.junit.jupiter.api.Test;
import com.cashly.cashly_api.architecture.BusinessModulePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** Keeps area grouping explicit without treating an area as a business module. */
class ErpAreaStructureTest {
    private static final Path MAIN = Path.of("src/main/java/com/cashly/cashly_api");
    private static final Path TEST = Path.of("src/test/java/com/cashly/cashly_api");
    private static final Map<String, String> MODULES = Map.ofEntries(
            Map.entry("quotes", "sales/quotes"), Map.entry("orders", "sales/orders"),
            Map.entry("purchases", "procurement/purchases"),
            Map.entry("products", "catalog/products"), Map.entry("categories", "catalog/categories"),
            Map.entry("price_types", "catalog/price_types"),
            Map.entry("warehouses", "inventory/warehouses"),
            Map.entry("invoices", "finance/billing/invoices"), Map.entry("returns", "finance/billing/returns"),
            Map.entry("credit_notes", "finance/billing/credit_notes"), Map.entry("debit_notes", "finance/billing/debit_notes"),
            Map.entry("payments", "finance/treasury/payments"), Map.entry("cash_register", "finance/treasury/cash_register"),
            Map.entry("receivables", "finance/treasury/receivables"), Map.entry("payment_methods", "finance/treasury/payment_methods"),
            Map.entry("customers", "partners/customers"), Map.entry("suppliers", "partners/suppliers"),
            Map.entry("dashboard", "analytics/dashboard"),
            Map.entry("auth", "platform/auth"), Map.entry("notifications", "platform/notifications"));
    private static final Set<String> ROOT_EXCEPTIONS = Set.of("shared", "infrastructure", "architecture");

    @Test
    void every_production_business_source_belongs_to_its_declared_area_and_module() throws IOException {
        assertThat(sources(MAIN).stream().filter(path -> !isApproved(path)).toList()).isEmpty();
    }

    @Test
    void every_test_business_source_belongs_to_its_declared_area_and_module() throws IOException {
        assertThat(sources(TEST).stream().filter(path -> !isApproved(path)).toList()).isEmpty();
    }

    @Test
    void all_existing_modules_retain_production_code_and_tests() throws IOException {
        Set<String> productionOwners = owners(sources(MAIN));
        Set<String> testOwners = owners(sources(TEST));
        assertThat(List.of(productionOwners, testOwners)).allSatisfy(owners ->
                assertThat(owners).containsExactlyInAnyOrderElementsOf(MODULES.keySet()));
    }

    @Test
    void rejects_returns_under_sales_and_old_ungrouped_module_paths() {
        assertThat(List.of("sales/returns/domain/entity/Return.java", "returns/domain/entity/Return.java",
                        "orders/application/create/use_case/CreateOrderUseCase.java",
                        "finance/invoices/domain/entity/Invoice.java", "sales/domain/entity/Order.java")
                .stream().map(Path::of).filter(ErpAreaStructureTest::isApproved).toList()).isEmpty();
    }

    @Test
    void accepts_owner_local_reporting_and_billing_returns() {
        assertThat(List.of("finance/billing/returns/domain/entity/Return.java",
                        "sales/orders/reporting/infrastructure/web/OrderReportController.java",
                        "inventory/warehouses/stock/domain/entity/Stock.java",
                        "platform/auth/user/domain/entity/User.java")
                .stream().map(Path::of).filter(path -> !isApproved(path)).toList()).isEmpty();
    }

    @Test
    void business_ownership_remains_distinct_inside_the_same_area() {
        assertThat(List.of(
                BusinessModulePaths.owner(Path.of("sales/orders/domain/entity/Order.java")),
                BusinessModulePaths.owner(Path.of("sales/quotes/domain/entity/Quote.java")),
                BusinessModulePaths.owner(Path.of("finance/billing/invoices/domain/entity/Invoice.java")),
                BusinessModulePaths.owner(Path.of("finance/billing/returns/domain/entity/Return.java"))))
                .containsExactly("orders", "quotes", "invoices", "returns");
    }

    private static boolean isApproved(Path path) {
        return path.toString().equals("CashlyApplication.java")
                || ROOT_EXCEPTIONS.contains(path.getName(0).toString())
                || MODULES.values().stream().map(Path::of).anyMatch(path::startsWith);
    }

    private static Set<String> owners(List<Path> sources) {
        return MODULES.entrySet().stream().filter(module -> sources.stream()
                        .anyMatch(path -> path.startsWith(Path.of(module.getValue()))))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    private static List<Path> sources(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            var result = stream.filter(path -> path.toString().endsWith(".java"))
                    .map(root::relativize).toList();
            assertThat(result).as("Structural scan under %s must inspect real sources", root).isNotEmpty();
            return result;
        }
    }
}
