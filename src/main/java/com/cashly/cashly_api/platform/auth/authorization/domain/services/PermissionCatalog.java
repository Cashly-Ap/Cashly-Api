package com.cashly.cashly_api.platform.auth.authorization.domain.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;

public final class PermissionCatalog {

    public record PermissionEntry(String code, String labelKey,
            String descriptionKey, int sortOrder) {

        public PermissionEntry {
            Objects.requireNonNull(code, "Permission code is required");
            Objects.requireNonNull(labelKey, "Permission label key is required");
            Objects.requireNonNull(descriptionKey,
                    "Permission description key is required");
        }
    }

    public record DomainCatalog(PermissionModule module, String labelKey,
            int sortOrder, List<PermissionEntry> permissions) {

        public DomainCatalog {
            Objects.requireNonNull(module, "Domain is required");
            Objects.requireNonNull(labelKey, "Domain label key is required");
            permissions = List.copyOf(Objects.requireNonNull(permissions,
                    "Domain permissions are required"));
        }
    }

    public record SubmoduleCatalog(String submodule, String labelKey,
            int sortOrder, List<DomainCatalog> domains) {

        public SubmoduleCatalog {
            Objects.requireNonNull(labelKey, "Submodule label key is required");
            domains = List.copyOf(Objects.requireNonNull(domains,
                    "Submodule domains are required"));
        }
    }

    public record AreaCatalog(String area, String labelKey, int sortOrder,
            List<SubmoduleCatalog> submodules) {

        public AreaCatalog {
            Objects.requireNonNull(area, "Area is required");
            Objects.requireNonNull(labelKey, "Area label key is required");
            submodules = List.copyOf(Objects.requireNonNull(submodules,
                    "Area submodules are required"));
        }
    }

    private static final String AREA_LABEL_PREFIX = "platform.auth.areas.";
    private static final String SUBMODULE_LABEL_PREFIX =
            "platform.auth.submodules.";
    private static final String DOMAIN_LABEL_PREFIX = "platform.auth.domains.";
    private static final String PERMISSION_LABEL_PREFIX =
            "platform.auth.permissions.";
    private static final String DESCRIPTION_SUFFIX = ".description";
    private static final String KEY_SEPARATOR = ".";

    private static final Map<PermissionAction, Integer> ACTION_ORDERS = actionOrders();

    private static final List<AreaCatalog> AREAS = build();

    private PermissionCatalog() {
    }

    public static List<AreaCatalog> areas() {
        return AREAS;
    }

    private static List<AreaCatalog> build() {
        var areas = List.of(
                area("SALES", "sales", 10,
                        submodule(null, "sales", 10,
                                domain(PermissionModule.SALES_QUOTES, 10),
                                domain(PermissionModule.SALES_ORDERS, 20))),
                area("PURCHASES", "purchases", 20,
                        submodule(null, "purchases", 10,
                                domain(PermissionModule.PURCHASES, 10))),
                area("CATALOG", "catalog", 30,
                        submodule(null, "catalog", 10,
                                domain(PermissionModule.CATALOG_PRODUCTS, 10),
                                domain(PermissionModule.CATALOG_CATEGORIES, 20),
                                domain(PermissionModule.CATALOG_PRICES, 30))),
                area("INVENTORY", "inventory", 40,
                        submodule(null, "inventory", 10,
                                domain(PermissionModule.INVENTORY_WAREHOUSES,
                                        10))),
                area("FINANCE", "finance", 50,
                        submodule("FINANCE_BILLING", "finance_billing", 10,
                                domain(PermissionModule.FINANCE_BILLING_INVOICES,
                                        10),
                                domain(PermissionModule.FINANCE_BILLING_RETURNS,
                                        20),
                                domain(PermissionModule.FINANCE_BILLING_CREDIT_NOTES,
                                        30),
                                domain(PermissionModule.FINANCE_BILLING_DEBIT_NOTES,
                                        40)),
                        submodule("FINANCE_TREASURY", "finance_treasury", 20,
                                domain(PermissionModule.FINANCE_TREASURY_CASH_REGISTER,
                                        10),
                                domain(PermissionModule.FINANCE_TREASURY_RECEIVABLES,
                                        20),
                                domain(PermissionModule.FINANCE_TREASURY_PAYMENT_METHODS,
                                        30))),
                area("CONTACTS", "contacts", 60,
                        submodule(null, "contacts", 10,
                                domain(PermissionModule.CONTACTS_CUSTOMERS, 10),
                                domain(PermissionModule.CONTACTS_SUPPLIERS, 20))),
                area("ANALYTICS", "analytics", 70,
                        submodule(null, "analytics", 10,
                                domain(PermissionModule.DASHBOARD, 10))),
                area("PLATFORM", "platform", 90,
                        submodule(null, "platform", 10,
                                domain(PermissionModule.USERS, 10),
                                domain(PermissionModule.ROLES, 20),
                                domain(PermissionModule.DEPARTMENTS, 30),
                                domain(PermissionModule.COMPANY, 40))));
        validate(areas);
        return ordered(areas);
    }

    private static AreaCatalog area(String area, String areaKey, int sortOrder,
            SubmoduleCatalog... submodules) {
        return new AreaCatalog(area, AREA_LABEL_PREFIX + areaKey, sortOrder,
                List.of(submodules));
    }

    private static SubmoduleCatalog submodule(String submodule,
            String submoduleKey, int sortOrder, DomainCatalog... domains) {
        return new SubmoduleCatalog(submodule,
                SUBMODULE_LABEL_PREFIX + submoduleKey, sortOrder,
                List.of(domains));
    }

    private static DomainCatalog domain(PermissionModule module,
            int sortOrder) {
        var actions = PermissionRegistry.modules().get(module);
        var permissions = new ArrayList<PermissionEntry>(actions.size());
        for (var action : actions) {
            var labelKey = PERMISSION_LABEL_PREFIX + moduleKey(module)
                    + KEY_SEPARATOR + actionKey(action);
            permissions.add(new PermissionEntry(
                    module.name() + "_" + action.name(), labelKey,
                    labelKey + DESCRIPTION_SUFFIX, ACTION_ORDERS.get(action)));
        }
        return new DomainCatalog(module, DOMAIN_LABEL_PREFIX + moduleKey(module),
                sortOrder, permissions);
    }

    private static Map<PermissionAction, Integer> actionOrders() {
        var orders = new EnumMap<PermissionAction, Integer>(
                PermissionAction.class);
        orders.put(PermissionAction.VIEW_LIST, 10);
        orders.put(PermissionAction.VIEW_DETAIL, 20);
        orders.put(PermissionAction.EDIT, 30);
        orders.put(PermissionAction.DELETE, 40);
        orders.put(PermissionAction.CREATE, 50);
        orders.put(PermissionAction.REPORT_PDF, 60);
        orders.put(PermissionAction.DOWNLOAD_PDF, 70);
        orders.put(PermissionAction.COMMERCIAL_CATALOG_PDF, 80);
        orders.put(PermissionAction.CONVERT_TO_ORDER, 90);
        orders.put(PermissionAction.CONVERT_TO_INVOICE, 100);
        orders.put(PermissionAction.CONFIRM, 110);
        orders.put(PermissionAction.CANCEL, 120);
        orders.put(PermissionAction.DUPLICATE, 130);
        orders.put(PermissionAction.VIEW_STOCK, 140);
        orders.put(PermissionAction.MOVEMENT_HISTORY, 150);
        orders.put(PermissionAction.RECORD_ENTRY, 160);
        orders.put(PermissionAction.RECORD_EXIT, 170);
        orders.put(PermissionAction.REPORT_STOCK, 180);
        orders.put(PermissionAction.REPORT_MOVEMENTS, 190);
        orders.put(PermissionAction.CASH_HISTORY, 200);
        orders.put(PermissionAction.REPORT_CASH_REGISTER, 210);
        orders.put(PermissionAction.OPEN, 220);
        orders.put(PermissionAction.CLOSE, 230);
        orders.put(PermissionAction.VIEW_DEBT, 240);
        orders.put(PermissionAction.PAY_ALL, 250);
        orders.put(PermissionAction.PARTIAL_PAYMENT, 260);
        orders.put(PermissionAction.PAY_DIRECT, 270);
        orders.put(PermissionAction.PAY_WITH_VALIDATION, 280);
        orders.put(PermissionAction.REFUND, 290);
        orders.put(PermissionAction.ADVANCE, 300);
        orders.put(PermissionAction.APPROVE_PAYMENT, 310);
        orders.put(PermissionAction.REJECT_PAYMENT, 320);
        orders.put(PermissionAction.VIEW_BALANCE, 330);
        orders.put(PermissionAction.BALANCE, 340);
        orders.put(PermissionAction.VIEW, 10);
        return orders;
    }

    private static List<AreaCatalog> ordered(List<AreaCatalog> areas) {
        var sortedAreas = new ArrayList<>(areas);
        sortedAreas.sort(Comparator.comparingInt(AreaCatalog::sortOrder)
                .thenComparing(AreaCatalog::area));
        var result = new ArrayList<AreaCatalog>(sortedAreas.size());
        for (var area : sortedAreas) {
            var submodules = new ArrayList<>(area.submodules());
            submodules.sort(Comparator
                    .comparingInt(SubmoduleCatalog::sortOrder)
                    .thenComparing(submodule -> submodule.submodule() == null
                            ? ""
                            : submodule.submodule()));
            var orderedSubmodules = new ArrayList<SubmoduleCatalog>(
                    submodules.size());
            for (var submodule : submodules) {
                var domains = new ArrayList<>(submodule.domains());
                domains.sort(Comparator.comparingInt(DomainCatalog::sortOrder)
                        .thenComparing(domain -> domain.module().name()));
                var orderedDomains = new ArrayList<DomainCatalog>(domains.size());
                for (var domain : domains) {
                    var permissions = new ArrayList<>(domain.permissions());
                    permissions.sort(Comparator
                            .comparingInt(PermissionEntry::sortOrder)
                            .thenComparing(PermissionEntry::code));
                    orderedDomains.add(new DomainCatalog(domain.module(),
                            domain.labelKey(), domain.sortOrder(), permissions));
                }
                orderedSubmodules.add(new SubmoduleCatalog(
                        submodule.submodule(), submodule.labelKey(),
                        submodule.sortOrder(), orderedDomains));
            }
            result.add(new AreaCatalog(area.area(), area.labelKey(),
                    area.sortOrder(), orderedSubmodules));
        }
        return List.copyOf(result);
    }

    private static void validate(List<AreaCatalog> areas) {
        var classified = new HashSet<PermissionModule>();
        var seenAreas = new HashSet<String>();
        for (var area : areas) {
            if (!seenAreas.add(area.area()))
                throw new IllegalStateException(
                        "Area declared twice: " + area.area());
            for (var submodule : area.submodules()) {
                for (var domain : submodule.domains()) {
                    if (!classified.add(domain.module()))
                        throw new IllegalStateException(
                                "Module classified twice: "
                                        + domain.module().name());
                    var registered = PermissionRegistry.modules()
                            .getOrDefault(domain.module(), Set.of());
                    var catalogue = domain.permissions().stream()
                            .map(PermissionEntry::code).toList();
                    registered.forEach(action -> {
                        var code = domain.module().name() + "_"
                                + action.name();
                        if (!catalogue.contains(code))
                            throw new IllegalStateException(
                                    "Registry permission without catalog entry: "
                                            + code);
                    });
                }
            }
        }
        PermissionRegistry.modules().keySet().forEach(module -> {
            if (!classified.contains(module))
                throw new IllegalStateException(
                        "Registry module without catalog entry: "
                                + module.name());
        });
    }

    private static String moduleKey(PermissionModule module) {
        return switch (module) {
            case CONTACTS_CUSTOMERS -> "customers";
            case CONTACTS_SUPPLIERS -> "suppliers";
            default -> module.name().toLowerCase(Locale.ROOT);
        };
    }

    private static String actionKey(PermissionAction action) {
        return action.name().toLowerCase(Locale.ROOT);
    }
}
