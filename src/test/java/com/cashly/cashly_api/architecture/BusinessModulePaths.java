package com.cashly.cashly_api.architecture;

import java.nio.file.Path;
import java.util.Map;

/** Resolves the business owner rather than treating an ERP area as one module. */
public final class BusinessModulePaths {
    private static final Map<String, String> MODULES = Map.ofEntries(
            Map.entry("auth", "auth"),
            Map.entry("cash_register", "cash_register"),
            Map.entry("categories", "categories"),
            Map.entry("credit_notes", "credit_notes"),
            Map.entry("customers", "customers"),
            Map.entry("dashboard", "dashboard"),
            Map.entry("debit_notes", "debit_notes"),
            Map.entry("invoices", "invoices"),
            Map.entry("notifications", "notifications"),
            Map.entry("orders", "orders"),
            Map.entry("payment_methods", "payment_methods"),
            Map.entry("payments", "payments"),
            Map.entry("price_types", "price_types"),
            Map.entry("products", "products"),
            Map.entry("purchases", "purchases"),
            Map.entry("quotes", "quotes"),
            Map.entry("receivables", "receivables"),
            Map.entry("returns", "returns"),
            Map.entry("suppliers", "suppliers"),
            Map.entry("warehouses", "warehouses"));

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
