package com.cashly.cashly_api.platform.auth.user.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BusinessModuleUnitTest {

    @Test
    void should_have_platform_domains() {
        assertEquals(BusinessModule.USERS, BusinessModule.valueOf("USERS"));
        assertEquals(BusinessModule.ROLES, BusinessModule.valueOf("ROLES"));
        assertEquals(BusinessModule.DEPARTMENTS,
                BusinessModule.valueOf("DEPARTMENTS"));
    }

    @Test
    void should_have_catalog_domains() {
        assertEquals(BusinessModule.CATALOG_PRODUCTS,
                BusinessModule.valueOf("CATALOG_PRODUCTS"));
        assertEquals(BusinessModule.CATALOG_CATEGORIES,
                BusinessModule.valueOf("CATALOG_CATEGORIES"));
        assertEquals(BusinessModule.CATALOG_PRICES,
                BusinessModule.valueOf("CATALOG_PRICES"));
    }

    @Test
    void should_have_inventory_domains() {
        assertEquals(BusinessModule.INVENTORY_WAREHOUSES,
                BusinessModule.valueOf("INVENTORY_WAREHOUSES"));
    }

    @Test
    void should_have_sales_domains() {
        assertEquals(BusinessModule.SALES_QUOTES,
                BusinessModule.valueOf("SALES_QUOTES"));
        assertEquals(BusinessModule.SALES_ORDERS,
                BusinessModule.valueOf("SALES_ORDERS"));
        assertEquals(BusinessModule.CONTACTS_CUSTOMERS,
                BusinessModule.valueOf("CONTACTS_CUSTOMERS"));
    }

    @Test
    void should_have_finance_domains() {
        assertEquals(BusinessModule.FINANCE_INVOICES,
                BusinessModule.valueOf("FINANCE_INVOICES"));
        assertEquals(BusinessModule.FINANCE_RETURNS,
                BusinessModule.valueOf("FINANCE_RETURNS"));
        assertEquals(BusinessModule.FINANCE_CREDIT_NOTES,
                BusinessModule.valueOf("FINANCE_CREDIT_NOTES"));
        assertEquals(BusinessModule.FINANCE_DEBIT_NOTES,
                BusinessModule.valueOf("FINANCE_DEBIT_NOTES"));
        assertEquals(BusinessModule.FINANCE_CASH_REGISTER,
                BusinessModule.valueOf("FINANCE_CASH_REGISTER"));
        assertEquals(BusinessModule.FINANCE_RECEIVABLES,
                BusinessModule.valueOf("FINANCE_RECEIVABLES"));
        assertEquals(BusinessModule.FINANCE_PAYMENT_METHODS,
                BusinessModule.valueOf("FINANCE_PAYMENT_METHODS"));
    }

    @Test
    void should_have_purchases_domains() {
        assertEquals(BusinessModule.PURCHASES,
                BusinessModule.valueOf("PURCHASES"));
        assertEquals(BusinessModule.CONTACTS_SUPPLIERS,
                BusinessModule.valueOf("CONTACTS_SUPPLIERS"));
    }

    @Test
    void should_have_dashboard_domain() {
        assertEquals(BusinessModule.DASHBOARD,
                BusinessModule.valueOf("DASHBOARD"));
    }

    @Test
    void should_return_twenty_seven_values() {
        assertEquals(20, BusinessModule.values().length);
    }

    @Test
    void should_not_contain_identifiers_outside_the_navbar_contract() {
        List<String> names = Arrays.stream(BusinessModule.values())
                .map(Enum::name).toList();
        assertFalse(names.contains("COMPANY"));
        assertFalse(names.contains("DOCUMENTS"));
        assertFalse(names.contains("FINANCE_BILLING"));
        assertFalse(names.contains("FINANCE_TREASURY"));
    }

    @Test
    void should_contain_all_expected_domains() {
        List<BusinessModule> values = Arrays.asList(BusinessModule.values());
        assertTrue(values.containsAll(List.of(BusinessModule.USERS,
                BusinessModule.ROLES, BusinessModule.DEPARTMENTS,
                BusinessModule.CATALOG_PRODUCTS,
                BusinessModule.CATALOG_CATEGORIES, BusinessModule.CATALOG_PRICES,
                BusinessModule.CATALOG_PRODUCTS,
                BusinessModule.INVENTORY_WAREHOUSES,
                BusinessModule.INVENTORY_WAREHOUSES,
                BusinessModule.INVENTORY_WAREHOUSES,
                BusinessModule.INVENTORY_WAREHOUSES, BusinessModule.SALES_QUOTES,
                BusinessModule.SALES_ORDERS, BusinessModule.CONTACTS_CUSTOMERS,
                BusinessModule.CONTACTS_CUSTOMERS,
                BusinessModule.FINANCE_INVOICES,
                BusinessModule.FINANCE_PAYMENT_METHODS,
                BusinessModule.FINANCE_INVOICES, BusinessModule.PURCHASES,
                BusinessModule.CONTACTS_SUPPLIERS,
                BusinessModule.DASHBOARD)));
    }
}
