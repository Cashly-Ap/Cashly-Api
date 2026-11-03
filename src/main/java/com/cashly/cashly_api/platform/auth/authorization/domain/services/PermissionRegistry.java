package com.cashly.cashly_api.platform.auth.authorization.domain.services;

import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.ADVANCE;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.APPROVE_PAYMENT;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.BALANCE;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.CANCEL;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.CASH_HISTORY;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.CLOSE;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.COMMERCIAL_CATALOG_PDF;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.CONFIRM;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.CONVERT_TO_INVOICE;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.CONVERT_TO_ORDER;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.CREATE;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.DELETE;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.DOWNLOAD_PDF;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.DUPLICATE;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.EDIT;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.MOVEMENT_HISTORY;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.OPEN;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.PARTIAL_PAYMENT;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.PAY_ALL;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.PAY_DIRECT;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.PAY_WITH_VALIDATION;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.RECORD_ENTRY;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.RECORD_EXIT;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.REFUND;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.REJECT_PAYMENT;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.REPORT_CASH_REGISTER;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.REPORT_MOVEMENTS;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.REPORT_PDF;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.REPORT_STOCK;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.VIEW;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.VIEW_BALANCE;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.VIEW_DEBT;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.VIEW_DETAIL;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.VIEW_LIST;
import static com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction.VIEW_STOCK;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;

public final class PermissionRegistry {

    private static final Map<PermissionModule, Set<PermissionAction>> MODULES = build();

    private PermissionRegistry() {
    }

    private static Map<PermissionModule, Set<PermissionAction>> build() {
        var result = new EnumMap<PermissionModule, Set<PermissionAction>>(
                PermissionModule.class);
        result.put(PermissionModule.SALES_QUOTES, EnumSet.of(VIEW_LIST,
                VIEW_DETAIL, EDIT, DELETE, CREATE, REPORT_PDF, DOWNLOAD_PDF,
                CONVERT_TO_ORDER, CONVERT_TO_INVOICE, CANCEL));
        result.put(PermissionModule.SALES_ORDERS, EnumSet.of(VIEW_LIST,
                VIEW_DETAIL, EDIT, DELETE, CREATE, REPORT_PDF, DOWNLOAD_PDF,
                CONFIRM, CONVERT_TO_INVOICE, CANCEL));
        result.put(PermissionModule.PURCHASES, EnumSet.of(VIEW_LIST, VIEW_DETAIL,
                EDIT, DELETE, CREATE, REPORT_PDF, DOWNLOAD_PDF, CONFIRM,
                CANCEL));
        result.put(PermissionModule.CATALOG_PRODUCTS, EnumSet.of(VIEW_LIST,
                VIEW_DETAIL, EDIT, DELETE, CREATE,
                COMMERCIAL_CATALOG_PDF));
        result.put(PermissionModule.CATALOG_CATEGORIES, EnumSet.of(VIEW_LIST,
                VIEW_DETAIL, EDIT, DELETE, CREATE));
        result.put(PermissionModule.CATALOG_PRICES, EnumSet.of(VIEW_LIST,
                VIEW_DETAIL, EDIT, DELETE, CREATE));
        result.put(PermissionModule.INVENTORY_WAREHOUSES, EnumSet.of(VIEW_LIST,
                VIEW_DETAIL, EDIT, DELETE, CREATE, VIEW_STOCK, MOVEMENT_HISTORY,
                RECORD_ENTRY, RECORD_EXIT, REPORT_STOCK, REPORT_MOVEMENTS));
        result.put(PermissionModule.FINANCE_BILLING_INVOICES, EnumSet.of(
                VIEW_LIST, VIEW_DETAIL, EDIT, DELETE, CREATE, REPORT_PDF,
                DOWNLOAD_PDF, CONFIRM, CANCEL, DUPLICATE));
        result.put(PermissionModule.FINANCE_BILLING_RETURNS, EnumSet.of(
                VIEW_LIST, VIEW_DETAIL, DELETE, CREATE));
        result.put(PermissionModule.FINANCE_BILLING_CREDIT_NOTES, EnumSet.of(
                VIEW_LIST, VIEW_DETAIL, DELETE, CREATE));
        result.put(PermissionModule.FINANCE_BILLING_DEBIT_NOTES, EnumSet.of(
                VIEW_LIST, VIEW_DETAIL, DELETE, CREATE));
        result.put(PermissionModule.FINANCE_TREASURY_CASH_REGISTER, EnumSet.of(
                VIEW_LIST, CASH_HISTORY, REPORT_CASH_REGISTER, OPEN, CLOSE));
        result.put(PermissionModule.FINANCE_TREASURY_RECEIVABLES, EnumSet.of(
                VIEW_LIST, VIEW_DEBT, REPORT_PDF, PAY_ALL, PARTIAL_PAYMENT,
                PAY_DIRECT, PAY_WITH_VALIDATION, REFUND, ADVANCE,
                APPROVE_PAYMENT, REJECT_PAYMENT, EDIT, DELETE));
        result.put(PermissionModule.FINANCE_TREASURY_PAYMENT_METHODS, EnumSet.of(
                VIEW_LIST, VIEW_DETAIL, EDIT, DELETE, CREATE));
        result.put(PermissionModule.CONTACTS_CUSTOMERS, EnumSet.of(VIEW_LIST,
                VIEW_DETAIL, EDIT, DELETE, CREATE, VIEW_BALANCE, BALANCE));
        result.put(PermissionModule.CONTACTS_SUPPLIERS, EnumSet.of(VIEW_LIST,
                VIEW_DETAIL, EDIT, DELETE, CREATE));
        result.put(PermissionModule.DASHBOARD, EnumSet.of(VIEW));
        result.put(PermissionModule.USERS, EnumSet.of(VIEW_LIST, VIEW_DETAIL,
                EDIT, DELETE, CREATE));
        result.put(PermissionModule.ROLES, EnumSet.of(VIEW_LIST, VIEW_DETAIL,
                EDIT, DELETE, CREATE));
        result.put(PermissionModule.DEPARTMENTS, EnumSet.of(VIEW_LIST,
                VIEW_DETAIL, EDIT, DELETE, CREATE));
        result.put(PermissionModule.COMPANY, EnumSet.of(VIEW, EDIT));
        return Collections.unmodifiableMap(result);
    }

    public static Map<PermissionModule, Set<PermissionAction>> modules() {
        return MODULES;
    }

    public static Set<String> codes() {
        return MODULES.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(
                        action -> entry.getKey().name() + "_" + action.name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Map<PermissionModule, Set<PermissionAction>> validate(
            Map<PermissionModule, Set<PermissionAction>> grants) {
        if (grants == null)
            throw new IllegalArgumentException("Permissions are required");
        var result = new EnumMap<PermissionModule, Set<PermissionAction>>(
                PermissionModule.class);
        grants.forEach((module, actions) -> {
            if (module == null || actions == null
                    || !modules().getOrDefault(module, Set.of())
                            .containsAll(actions))
                throw new IllegalArgumentException("Unsupported permission");
            if (!actions.isEmpty())
                result.put(module, Set.copyOf(actions));
        });
        return Collections.unmodifiableMap(result);
    }
}
