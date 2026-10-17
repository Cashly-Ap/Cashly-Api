package com.cashly.cashly_api.architecture;

import java.nio.file.Path;
import java.util.Map;

/** Resolves the business owner rather than treating an ERP area as one module. */
public final class BusinessModulePaths {
    private static final Map<String, String> MODULES = Map.ofEntries(
            Map.entry("auth", "platform/auth"),
            Map.entry("cash_register", "finance/treasury/cash_register"),
            Map.entry("categories", "catalog/categories"),
            Map.entry("credit_notes", "finance/billing/credit_notes"),
            Map.entry("customers", "partners/customers"),
            Map.entry("dashboard", "analytics/dashboard"),
            Map.entry("debit_notes", "finance/billing/debit_notes"),
            Map.entry("invoices", "finance/billing/invoices"),
            Map.entry("notifications", "platform/notifications"),
            Map.entry("orders", "sales/orders"),
            Map.entry("payment_methods", "finance/treasury/payment_methods"),
            Map.entry("payments", "finance/treasury/payments"),
            Map.entry("price_types", "catalog/price_types"),
            Map.entry("products", "catalog/products"),
            Map.entry("purchases", "procurement/purchases"),
            Map.entry("quotes", "sales/quotes"),
            Map.entry("receivables", "finance/treasury/receivables"),
            Map.entry("returns", "finance/billing/returns"),
            Map.entry("suppliers", "partners/suppliers"),
            Map.entry("warehouses", "inventory/warehouses"));

    private BusinessModulePaths() {}

    public static Path modulePath(String owner) {
        return Path.of(MODULES.getOrDefault(owner, owner));
    }

    public static Path withoutArea(Path path) {
        for (var entry : MODULES.entrySet()) {
            Path prefix = Path.of(entry.getValue());
            if (path.startsWith(prefix)) {
                return Path.of(entry.getKey()).resolve(prefix.relativize(path));
            }
        }
        return path;
    }

    public static String owner(Path path) {
        return withoutArea(path).getName(0).toString();
    }

    public static String ownerOfReference(String reference) {
        return owner(Path.of(reference.substring("com.cashly.cashly_api.".length()).replace('.', '/')));
    }
}
