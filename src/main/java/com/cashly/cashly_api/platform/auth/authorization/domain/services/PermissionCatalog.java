package com.cashly.cashly_api.platform.auth.authorization.domain.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;

public final class PermissionCatalog {

    public record PermissionEntry(PermissionAction action, String labelKey,
            String descriptionKey, int sortOrder) {

        public PermissionEntry {
            Objects.requireNonNull(action, "Permission action is required");
            requireText(labelKey, "Permission label key is required");
            requireText(descriptionKey,
                    "Permission description key is required");
        }
    }

    public record DomainCatalog(BusinessModule module, String labelKey,
            int sortOrder, List<PermissionEntry> permissions) {

        public DomainCatalog {
            Objects.requireNonNull(module, "Domain is required");
            requireText(labelKey, "Domain label key is required");
            permissions = List.copyOf(Objects.requireNonNull(permissions,
                    "Domain permissions are required"));
        }
    }

    public record AreaCatalog(String area, String labelKey, int sortOrder,
            List<DomainCatalog> domains) {

        public AreaCatalog {
            requireText(area, "Area is required");
            requireText(labelKey, "Area label key is required");
            domains = List.copyOf(
                    Objects.requireNonNull(domains, "Area domains are required"));
        }
    }

    private static final String AREA_LABEL_PREFIX = "platform.auth.areas.";
    private static final String DOMAIN_LABEL_PREFIX = "platform.auth.domains.";
    private static final String PERMISSION_LABEL_PREFIX =
            "platform.auth.permissions.";
    private static final String DESCRIPTION_SUFFIX = ".description";
    private static final String KEY_SEPARATOR = ".";

    private static final int SALES_AREA_ORDER = 10;
    private static final int PURCHASES_AREA_ORDER = 20;
    private static final int CATALOG_AREA_ORDER = 30;
    private static final int INVENTORY_AREA_ORDER = 40;
    private static final int FINANCE_AREA_ORDER = 50;
    private static final int CONTACTS_AREA_ORDER = 60;
    private static final int ANALYTICS_AREA_ORDER = 70;
    private static final int PLATFORM_AREA_ORDER = 90;

    private static final int FIRST_DOMAIN_ORDER = 10;
    private static final int SECOND_DOMAIN_ORDER = 20;
    private static final int THIRD_DOMAIN_ORDER = 30;
    private static final int FOURTH_DOMAIN_ORDER = 40;
    private static final int FIFTH_DOMAIN_ORDER = 50;
    private static final int SIXTH_DOMAIN_ORDER = 60;
    private static final int SEVENTH_DOMAIN_ORDER = 70;
    private static final int EIGHTH_DOMAIN_ORDER = 80;
    private static final int NINTH_DOMAIN_ORDER = 90;
    private static final int TENTH_DOMAIN_ORDER = 100;

    private static final int READ_ORDER = 10;
    private static final int CREATE_ORDER = 20;
    private static final int UPDATE_ORDER = 30;
    private static final int DELETE_ORDER = 40;
    private static final int EXPORT_ORDER = 50;
    private static final int IMPORT_ORDER = 60;
    private static final int EXECUTE_ORDER = 70;
    private static final int CONFIRM_ORDER = 80;
    private static final int CANCEL_ORDER = 90;
    private static final int DUPLICATE_ORDER = 100;
    private static final int DETAILS_ORDER = 110;
    private static final int BALANCE_ORDER = 120;
    private static final int HISTORY_ORDER = 130;
    private static final int OPEN_ORDER = 140;
    private static final int CLOSE_ORDER = 150;
    private static final int REPORT_ORDER = 160;
    private static final int PAY_ALL_ORDER = 170;
    private static final int PARTIAL_PAYMENT_ORDER = 180;
    private static final int REFUND_ORDER = 190;
    private static final int ADVANCE_ORDER = 200;
    private static final int PAY_WITH_VALIDATION_ORDER = 210;
    private static final int PAY_DIRECT_ORDER = 220;
    private static final int APPROVE_PAYMENT_ORDER = 230;
    private static final int REJECT_PAYMENT_ORDER = 240;

    private static final Map<PermissionAction, Integer> ACTION_ORDERS =
            actionOrders();

    private static final List<AreaCatalog> AREAS = build();

    private PermissionCatalog() {
    }

    public static List<AreaCatalog> areas() {
        return AREAS;
    }

    private static List<AreaCatalog> build() {
        var declared = declare();
        validate(declared);
        return ordered(declared);
    }

    private static List<AreaCatalog> declare() {
        return List.of(
                area("SALES", "sales", SALES_AREA_ORDER,
                        domain(BusinessModule.SALES_QUOTES,
                                FIRST_DOMAIN_ORDER, PermissionAction.READ,
                                PermissionAction.CREATE, PermissionAction.UPDATE,
                                PermissionAction.DELETE, PermissionAction.CONFIRM,
                                PermissionAction.EXPORT),
                        domain(BusinessModule.SALES_ORDERS,
                                SECOND_DOMAIN_ORDER, PermissionAction.READ,
                                PermissionAction.CREATE, PermissionAction.UPDATE,
                                PermissionAction.DELETE, PermissionAction.CONFIRM,
                                PermissionAction.CANCEL, PermissionAction.EXPORT)),
                area("PURCHASES", "purchases", PURCHASES_AREA_ORDER,
                        domain(BusinessModule.PURCHASES,
                                FIRST_DOMAIN_ORDER, PermissionAction.READ,
                                PermissionAction.CREATE, PermissionAction.UPDATE,
                                PermissionAction.DELETE, PermissionAction.CONFIRM,
                                PermissionAction.CANCEL, PermissionAction.EXPORT)),
                area("CATALOG", "catalog", CATALOG_AREA_ORDER,
                        domain(BusinessModule.CATALOG_PRODUCTS,
                                 FIRST_DOMAIN_ORDER, PermissionAction.READ, PermissionAction.DETAILS,
                                 PermissionAction.UPDATE, PermissionAction.CREATE,
                                 PermissionAction.REPORT_PRODUCTS, PermissionAction.REPORT_COMMERCIAL_CATALOG),
                        domain(BusinessModule.CATALOG_CATEGORIES,
                                THIRD_DOMAIN_ORDER, crud()),
                        domain(BusinessModule.CATALOG_PRICES,
                                FOURTH_DOMAIN_ORDER, crud())),
                area("INVENTORY", "inventory", INVENTORY_AREA_ORDER,
                        domain(BusinessModule.INVENTORY_WAREHOUSES,
                                 FIRST_DOMAIN_ORDER, PermissionAction.READ, PermissionAction.DETAILS,
                                 PermissionAction.UPDATE, PermissionAction.CREATE, PermissionAction.STOCK,
                                 PermissionAction.MOVEMENT_HISTORY, PermissionAction.RECORD_ENTRY,
                                 PermissionAction.RECORD_EXIT, PermissionAction.REPORT_STOCK,
                                 PermissionAction.REPORT_MOVEMENTS)),
                area("FINANCE", "finance", FINANCE_AREA_ORDER,
                        domain(BusinessModule.FINANCE_INVOICES,
                                 FIRST_DOMAIN_ORDER, PermissionAction.READ, PermissionAction.DETAILS,
                                 PermissionAction.UPDATE, PermissionAction.CREATE,
                                 PermissionAction.REPORT_INVOICES, PermissionAction.CONFIRM,
                                 PermissionAction.CANCEL, PermissionAction.DUPLICATE),
                        domain(BusinessModule.FINANCE_RETURNS,
                                THIRD_DOMAIN_ORDER, crud()),
                        domain(BusinessModule.FINANCE_CREDIT_NOTES,
                                FOURTH_DOMAIN_ORDER, crud()),
                        domain(BusinessModule.FINANCE_DEBIT_NOTES,
                                FIFTH_DOMAIN_ORDER, crud()),
                        domain(BusinessModule.FINANCE_CASH_REGISTER,
                                 SIXTH_DOMAIN_ORDER, PermissionAction.READ, PermissionAction.DETAILS,
                                 PermissionAction.CASH_HISTORY, PermissionAction.OPEN,
                                 PermissionAction.CLOSE, PermissionAction.REPORT_CASH_REGISTER),
                        domain(BusinessModule.FINANCE_RECEIVABLES,
                                 EIGHTH_DOMAIN_ORDER, PermissionAction.READ, PermissionAction.DETAILS,
                                 PermissionAction.PAY_ALL,
                                PermissionAction.PARTIAL_PAYMENT,
                                PermissionAction.REFUND, PermissionAction.ADVANCE,
                                PermissionAction.PAY_WITH_VALIDATION,
                                PermissionAction.PAY_DIRECT,
                                PermissionAction.APPROVE_PAYMENT,
                                PermissionAction.REJECT_PAYMENT),
                        domain(BusinessModule.FINANCE_PAYMENT_METHODS,
                                NINTH_DOMAIN_ORDER, crud())),
                area("CONTACTS", "contacts", CONTACTS_AREA_ORDER,
                        domain(BusinessModule.CONTACTS_CUSTOMERS,
                                 FIRST_DOMAIN_ORDER, PermissionAction.READ, PermissionAction.DETAILS,
                                 PermissionAction.UPDATE, PermissionAction.CREATE,
                                 PermissionAction.CUSTOMER_BALANCE),
                        domain(BusinessModule.CONTACTS_SUPPLIERS,
                                THIRD_DOMAIN_ORDER, crud())),
                area("ANALYTICS", "analytics", ANALYTICS_AREA_ORDER,
                        domain(BusinessModule.DASHBOARD, FIRST_DOMAIN_ORDER,
                                PermissionAction.READ)),
                area("PLATFORM", "platform", PLATFORM_AREA_ORDER,
                        domain(BusinessModule.USERS, FIRST_DOMAIN_ORDER, crud()),
                        domain(BusinessModule.ROLES, SECOND_DOMAIN_ORDER, crud()),
                        domain(BusinessModule.DEPARTMENTS, THIRD_DOMAIN_ORDER,
                                crud())));
    }

    private static AreaCatalog area(String area, String areaKey, int sortOrder,
            DomainCatalog... domains) {
        return new AreaCatalog(area, AREA_LABEL_PREFIX + areaKey, sortOrder,
                List.of(domains));
    }

    private static DomainCatalog domain(BusinessModule module, int sortOrder,
            PermissionAction... actions) {
        var permissions = new ArrayList<PermissionEntry>(actions.length);
        for (var action : actions) {
            var labelKey = PERMISSION_LABEL_PREFIX + moduleKey(module)
                    + KEY_SEPARATOR + actionKey(action);
            permissions.add(new PermissionEntry(action, labelKey,
                    labelKey + DESCRIPTION_SUFFIX, ACTION_ORDERS.get(action)));
        }
        return new DomainCatalog(module, DOMAIN_LABEL_PREFIX + moduleKey(module),
                sortOrder, permissions);
    }

    private static PermissionAction[] crud() {
        return new PermissionAction[] {PermissionAction.READ,
                PermissionAction.CREATE, PermissionAction.UPDATE,
                PermissionAction.DELETE};
    }

    private static Map<PermissionAction, Integer> actionOrders() {
        var orders = new EnumMap<PermissionAction, Integer>(
                PermissionAction.class);
        orders.put(PermissionAction.READ, READ_ORDER);
        orders.put(PermissionAction.CREATE, CREATE_ORDER);
        orders.put(PermissionAction.UPDATE, UPDATE_ORDER);
        orders.put(PermissionAction.DELETE, DELETE_ORDER);
        orders.put(PermissionAction.EXPORT, EXPORT_ORDER);
        orders.put(PermissionAction.IMPORT, IMPORT_ORDER);
        orders.put(PermissionAction.EXECUTE, EXECUTE_ORDER);
        orders.put(PermissionAction.CONFIRM, CONFIRM_ORDER);
        orders.put(PermissionAction.CANCEL, CANCEL_ORDER);
        orders.put(PermissionAction.DUPLICATE, DUPLICATE_ORDER);
        orders.put(PermissionAction.DETAILS, DETAILS_ORDER);
        orders.put(PermissionAction.BALANCE, BALANCE_ORDER);
        orders.put(PermissionAction.HISTORY, HISTORY_ORDER);
        orders.put(PermissionAction.OPEN, OPEN_ORDER);
        orders.put(PermissionAction.CLOSE, CLOSE_ORDER);
        orders.put(PermissionAction.REPORT, REPORT_ORDER);
        orders.put(PermissionAction.PAY_ALL, PAY_ALL_ORDER);
        orders.put(PermissionAction.PARTIAL_PAYMENT, PARTIAL_PAYMENT_ORDER);
        orders.put(PermissionAction.REFUND, REFUND_ORDER);
        orders.put(PermissionAction.ADVANCE, ADVANCE_ORDER);
        orders.put(PermissionAction.PAY_WITH_VALIDATION, PAY_WITH_VALIDATION_ORDER);
        orders.put(PermissionAction.PAY_DIRECT, PAY_DIRECT_ORDER);
        orders.put(PermissionAction.APPROVE_PAYMENT, APPROVE_PAYMENT_ORDER);
        orders.put(PermissionAction.REJECT_PAYMENT, REJECT_PAYMENT_ORDER);
        orders.put(PermissionAction.CUSTOMER_BALANCE, 250);
        orders.put(PermissionAction.CASH_HISTORY, 260);
        orders.put(PermissionAction.REPORT_CASH_REGISTER, 270);
        orders.put(PermissionAction.REPORT_QUOTES, 271);
        orders.put(PermissionAction.REPORT_ORDERS, 272);
        orders.put(PermissionAction.REPORT_PURCHASES, 273);
        orders.put(PermissionAction.REPORT_PRODUCTS, 274);
        orders.put(PermissionAction.REPORT_COMMERCIAL_CATALOG, 275);
        orders.put(PermissionAction.REPORT_STOCK, 276);
        orders.put(PermissionAction.REPORT_MOVEMENTS, 277);
        orders.put(PermissionAction.REPORT_INVOICES, 278);
        orders.put(PermissionAction.REPORT_RECEIVABLES, 279);
        orders.put(PermissionAction.INVOICE_DUPLICATE, 280);
        orders.put(PermissionAction.STOCK, 290);
        orders.put(PermissionAction.MOVEMENT_HISTORY, 300);
        orders.put(PermissionAction.RECORD_ENTRY, 310);
        orders.put(PermissionAction.RECORD_EXIT, 320);
        return orders;
    }

    private static List<AreaCatalog> ordered(List<AreaCatalog> areas) {
        var sortedAreas = new ArrayList<>(areas);
        sortedAreas.sort(Comparator.comparingInt(AreaCatalog::sortOrder)
                .thenComparing(AreaCatalog::area));
        var result = new ArrayList<AreaCatalog>(sortedAreas.size());
        for (var area : sortedAreas) {
            var domains = new ArrayList<>(area.domains());
            domains.sort(Comparator.comparingInt(DomainCatalog::sortOrder)
                    .thenComparing(domain -> domain.module().name()));
            var orderedDomains = new ArrayList<DomainCatalog>(domains.size());
            for (var domain : domains) {
                var permissions = new ArrayList<>(domain.permissions());
                permissions.sort(Comparator
                        .comparingInt(PermissionEntry::sortOrder)
                        .thenComparing(entry -> entry.action().name()));
                orderedDomains.add(new DomainCatalog(domain.module(),
                        domain.labelKey(), domain.sortOrder(), permissions));
            }
            result.add(new AreaCatalog(area.area(), area.labelKey(),
                    area.sortOrder(), orderedDomains));
        }
        return List.copyOf(result);
    }

    private static void validate(List<AreaCatalog> areas) {
        var classified = new EnumMap<BusinessModule, Set<PermissionAction>>(
                BusinessModule.class);
        var seenAreas = new HashSet<String>();
        for (var area : areas) {
            if (!seenAreas.add(area.area()))
                throw new IllegalStateException(
                        "Area declared twice: " + area.area());
            for (var domain : area.domains()) {
                var perModule = classified.computeIfAbsent(domain.module(),
                        module -> new HashSet<>());
                for (var permission : domain.permissions()) {
                    if (!perModule.add(permission.action()))
                        throw new IllegalStateException("Permission classified twice: "
                                + domain.module().name() + ":"
                                + permission.action().name());
                }
            }
        }
        PermissionRegistry.modules().forEach((module, actions) -> {
            var catalogued = classified.getOrDefault(module, Set.of());
            for (var action : actions) {
                if (!catalogued.contains(action))
                    throw new IllegalStateException(
                            "Registry permission without catalog entry: "
                                    + module.name() + ":" + action.name());
            }
            for (var action : catalogued) {
                if (!actions.contains(action))
                    throw new IllegalStateException(
                            "Catalog permission is not registered: "
                                    + module.name() + ":" + action.name());
            }
        });
        classified.keySet().forEach(module -> {
            if (!PermissionRegistry.modules().containsKey(module))
                throw new IllegalStateException(
                        "Catalog domain is not registered: " + module.name());
        });
    }

    private static String moduleKey(BusinessModule module) {
        return switch (module) {
            case CONTACTS_CUSTOMERS -> "customers";
                        case CONTACTS_SUPPLIERS -> "suppliers";
            default -> module.name().toLowerCase(Locale.ROOT);
        };
    }

    private static String actionKey(PermissionAction action) {
        return action.name().toLowerCase(Locale.ROOT);
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(message);
    }
}
