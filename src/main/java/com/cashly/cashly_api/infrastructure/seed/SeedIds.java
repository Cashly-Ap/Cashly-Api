package com.cashly.cashly_api.infrastructure.seed;

import com.cashly.cashly_api.platform.company.domain.Company;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class SeedIds {

    public static final UUID COMPANY = Company.INSTALLATION_ID;
    public static final UUID SYSTEM_ADMIN_ROLE = id("role.system-admin");
    public static final UUID OPERATOR_ROLE = id("role.operator");
    public static final UUID ADMINISTRATION_DEPARTMENT = id(
            "department.administration");
    public static final UUID SALES_DEPARTMENT = id("department.sales");
    public static final UUID ADMIN_USER = id("user.admin");
    public static final UUID OPERATOR_USER = id("user.operator");
    public static final UUID CUSTOMER_ONE = id("customer.one");
    public static final UUID CUSTOMER_TWO = id("customer.two");
    public static final UUID CATEGORY_FOOD = id("category.food");
    public static final UUID CATEGORY_DRINKS = id("category.drinks");
    public static final UUID PRODUCT_COFFEE = id("product.coffee");
    public static final UUID PRODUCT_WATER = id("product.water");
    public static final UUID WAREHOUSE_MAIN = id("warehouse.main");
    public static final UUID WAREHOUSE_RESERVE = id("warehouse.reserve");
    public static final UUID ORDER_ONE = id("order.one");
    public static final UUID ORDER_TWO = id("order.two");
    public static final UUID INVOICE_ONE = id("invoice.one");
    public static final UUID INVOICE_TWO = id("invoice.two");
    public static final UUID QUOTE_ONE = id("quote.one");
    public static final UUID QUOTE_TWO = id("quote.two");
    public static final UUID PAYMENT_ONE = id("payment.one");
    public static final UUID PAYMENT_TWO = id("payment.two");
    public static final UUID RETURN_ONE = id("return.one");
    public static final UUID RETURN_TWO = id("return.two");

    private SeedIds() {
    }

    public static UUID id(String key) {
        return UUID.nameUUIDFromBytes(
                ("cashly-seed:" + key).getBytes(StandardCharsets.UTF_8));
    }
}
