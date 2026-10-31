package com.cashly.cashly_api.platform.auth.user.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleUnitTest {

    @Test
    void should_have_SUPER_ADMIN_value() {
        UserRole role = UserRole.valueOf("SUPER_ADMIN");
        assertEquals(UserRole.SUPER_ADMIN, role);
    }

    @Test
    void should_have_ADMIN_value() {
        UserRole role = UserRole.valueOf("ADMIN");
        assertEquals(UserRole.ADMIN, role);
    }

    @Test
    void should_have_OPERATOR_value() {
        UserRole role = UserRole.valueOf("OPERATOR");
        assertEquals(UserRole.OPERATOR, role);
    }

    @Test
    void should_return_three_values() {
        UserRole[] values = UserRole.values();
        assertEquals(3, values.length);
    }

    @Test
    void should_contain_all_expected_roles() {
        UserRole[] values = UserRole.values();
        assertTrue(
                java.util.Arrays.asList(values).contains(UserRole.SUPER_ADMIN));
        assertTrue(java.util.Arrays.asList(values).contains(UserRole.ADMIN));
        assertTrue(java.util.Arrays.asList(values).contains(UserRole.OPERATOR));
    }
}
