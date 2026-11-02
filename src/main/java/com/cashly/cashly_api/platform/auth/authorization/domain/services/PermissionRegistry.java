package com.cashly.cashly_api.platform.auth.authorization.domain.services;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;

public final class PermissionRegistry {

    private static final Set<PermissionAction> CRUD = EnumSet.of(
            PermissionAction.READ, PermissionAction.CREATE,
            PermissionAction.UPDATE, PermissionAction.DELETE);

    private static final Map<BusinessModule, Set<PermissionAction>> MODULES = build();

    private PermissionRegistry() {
    }

    private static Map<BusinessModule, Set<PermissionAction>> build() {
        var result = new EnumMap<BusinessModule, Set<PermissionAction>>(
                BusinessModule.class);
        result.put(BusinessModule.USERS, CRUD);
        result.put(BusinessModule.ROLES, CRUD);
        result.put(BusinessModule.DEPARTMENTS, CRUD);
        result.put(BusinessModule.CATALOG_PRODUCTS, EnumSet.of(PermissionAction.READ, PermissionAction.DETAILS, PermissionAction.UPDATE, PermissionAction.CREATE, PermissionAction.REPORT_PRODUCTS, PermissionAction.REPORT_COMMERCIAL_CATALOG));
        result.put(BusinessModule.CATALOG_CATEGORIES, CRUD);
        result.put(BusinessModule.CATALOG_PRICES, CRUD);
        result.put(BusinessModule.INVENTORY_WAREHOUSES, EnumSet.of(PermissionAction.READ, PermissionAction.DETAILS, PermissionAction.UPDATE, PermissionAction.CREATE, PermissionAction.STOCK, PermissionAction.MOVEMENT_HISTORY, PermissionAction.RECORD_ENTRY, PermissionAction.RECORD_EXIT, PermissionAction.REPORT_STOCK, PermissionAction.REPORT_MOVEMENTS));
        result.put(BusinessModule.SALES_QUOTES, EnumSet.of(
                PermissionAction.READ, PermissionAction.CREATE,
                PermissionAction.UPDATE, PermissionAction.DELETE,
                PermissionAction.CONFIRM, PermissionAction.EXPORT));
        result.put(BusinessModule.SALES_ORDERS, EnumSet.of(
                PermissionAction.READ, PermissionAction.CREATE,
                PermissionAction.UPDATE, PermissionAction.DELETE,
                 PermissionAction.CONFIRM, PermissionAction.CANCEL,
                 PermissionAction.EXPORT));
        result.put(BusinessModule.CONTACTS_CUSTOMERS, EnumSet.of(PermissionAction.READ, PermissionAction.DETAILS, PermissionAction.UPDATE, PermissionAction.CREATE, PermissionAction.CUSTOMER_BALANCE));
        result.put(BusinessModule.FINANCE_INVOICES, EnumSet.of(PermissionAction.READ, PermissionAction.DETAILS, PermissionAction.UPDATE, PermissionAction.CREATE, PermissionAction.REPORT_INVOICES, PermissionAction.CONFIRM, PermissionAction.CANCEL, PermissionAction.DUPLICATE));
        result.put(BusinessModule.FINANCE_RETURNS, CRUD);
        result.put(BusinessModule.FINANCE_CREDIT_NOTES, CRUD);
        result.put(BusinessModule.FINANCE_DEBIT_NOTES, CRUD);
        result.put(BusinessModule.FINANCE_CASH_REGISTER,
                EnumSet.of(PermissionAction.READ, PermissionAction.DETAILS, PermissionAction.CASH_HISTORY, PermissionAction.OPEN, PermissionAction.CLOSE, PermissionAction.REPORT_CASH_REGISTER));
        result.put(BusinessModule.FINANCE_RECEIVABLES,
                 EnumSet.of(PermissionAction.READ, PermissionAction.DETAILS,
                         PermissionAction.PAY_ALL, PermissionAction.PARTIAL_PAYMENT, PermissionAction.REFUND,
                        PermissionAction.ADVANCE,
                        PermissionAction.PAY_WITH_VALIDATION,
                        PermissionAction.PAY_DIRECT,
                        PermissionAction.APPROVE_PAYMENT,
                        PermissionAction.REJECT_PAYMENT));
        result.put(BusinessModule.FINANCE_PAYMENT_METHODS, CRUD);
        result.put(BusinessModule.PURCHASES, EnumSet.of(
                PermissionAction.READ, PermissionAction.CREATE,
                PermissionAction.UPDATE, PermissionAction.DELETE,
                PermissionAction.CONFIRM, PermissionAction.CANCEL,
                PermissionAction.EXPORT));
        result.put(BusinessModule.CONTACTS_SUPPLIERS, CRUD);
        result.put(BusinessModule.DASHBOARD,
                EnumSet.of(PermissionAction.READ));
        return Collections.unmodifiableMap(result);
    }

    public static Map<BusinessModule, Set<PermissionAction>> modules() {
        return MODULES;
    }

    public static Map<BusinessModule, Set<PermissionAction>> validate(
            Map<BusinessModule, Set<PermissionAction>> grants) {
        if (grants == null)
            throw new IllegalArgumentException("Permissions are required");
        var result = new EnumMap<BusinessModule, Set<PermissionAction>>(
                BusinessModule.class);
        grants.forEach((module, actions) -> {
            if (module == null || actions == null
                    || !modules().get(module).containsAll(actions))
                throw new IllegalArgumentException("Unsupported permission");
            if (module == BusinessModule.FINANCE_RECEIVABLES
                    && actions.contains(PermissionAction.PAY_WITH_VALIDATION)
                    && actions.contains(PermissionAction.PAY_DIRECT))
                throw new IllegalArgumentException(
                        "PAY_WITH_VALIDATION and PAY_DIRECT are mutually exclusive");
            if (!actions.isEmpty())
                result.put(module, Set.copyOf(actions));
        });
        return Collections.unmodifiableMap(result);
    }
}
