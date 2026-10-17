package com.cashly.cashly_api.platform.auth.user.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BusinessModuleUnitTest {

    @Test
    void should_have_USERS_value() {
        BusinessModule module = BusinessModule.valueOf("USERS");
        assertEquals(BusinessModule.USERS, module);
    }

    @Test
    void should_have_INVENTORY_value() {
        BusinessModule module = BusinessModule.valueOf("INVENTORY");
        assertEquals(BusinessModule.INVENTORY, module);
    }

    @Test
    void should_have_SALES_value() {
        BusinessModule module = BusinessModule.valueOf("SALES");
        assertEquals(BusinessModule.SALES, module);
    }

    @Test
    void should_have_FINANCES_value() {
        BusinessModule module = BusinessModule.valueOf("FINANCES");
        assertEquals(BusinessModule.FINANCES, module);
    }

    @Test
    void should_have_PURCHASES_value() {
        BusinessModule module = BusinessModule.valueOf("PURCHASES");
        assertEquals(BusinessModule.PURCHASES, module);
    }

    @Test
    void should_have_DOCUMENT_ADMIN_value() {
        BusinessModule module = BusinessModule.valueOf("DOCUMENT_ADMIN");
        assertEquals(BusinessModule.DOCUMENT_ADMIN, module);
    }

    @Test
    void should_return_eight_values() {
        BusinessModule[] values = BusinessModule.values();
        assertEquals(7, values.length);
    }

    @Test
    void should_contain_all_expected_modules() {
        List<BusinessModule> values = Arrays.asList(BusinessModule.values());
        assertTrue(values.contains(BusinessModule.USERS));
        assertTrue(values.contains(BusinessModule.INVENTORY));
        assertTrue(values.contains(BusinessModule.SALES));
        assertTrue(values.contains(BusinessModule.FINANCES));
        assertTrue(values.contains(BusinessModule.PURCHASES));
        assertTrue(values.contains(BusinessModule.DOCUMENT_ADMIN));
    }
}
