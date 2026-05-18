package com.parsun.parsun_api.customers.domain.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerWarehouseAccessUnitTest {

    @Test
    void should_create_with_current_timestamp() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID grantedBy = UUID.randomUUID();

        CustomerWarehouseAccess access = CustomerWarehouseAccess.create(customerId, warehouseId, grantedBy);

        assertNull(access.getId());
        assertEquals(customerId, access.getCustomerId());
        assertEquals(warehouseId, access.getWarehouseId());
        assertEquals(grantedBy, access.getGrantedBy());
        assertNotNull(access.getGrantedAt());
    }

    @Test
    void should_throw_on_null_customer_id() {
        assertThrows(IllegalArgumentException.class, () ->
                new CustomerWarehouseAccess(null, null, UUID.randomUUID(), LocalDateTime.now(), UUID.randomUUID()));
    }

    @Test
    void should_throw_on_null_warehouse_id() {
        assertThrows(IllegalArgumentException.class, () ->
                new CustomerWarehouseAccess(null, UUID.randomUUID(), null, LocalDateTime.now(), UUID.randomUUID()));
    }

    @Test
    void should_throw_on_null_granted_by() {
        assertThrows(IllegalArgumentException.class, () ->
                new CustomerWarehouseAccess(null, UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), null));
    }

    @Test
    void should_default_granted_at_to_now_if_null() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID grantedBy = UUID.randomUUID();

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        CustomerWarehouseAccess access = new CustomerWarehouseAccess(null, customerId, warehouseId, null, grantedBy);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(access.getGrantedAt());
        assertTrue(access.getGrantedAt().isAfter(before) || access.getGrantedAt().equals(before));
        assertTrue(access.getGrantedAt().isBefore(after) || access.getGrantedAt().equals(after));
    }

    @Test
    void should_use_provided_granted_at() {
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID grantedBy = UUID.randomUUID();
        LocalDateTime specificDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        CustomerWarehouseAccess access = new CustomerWarehouseAccess(null, customerId, warehouseId, specificDate, grantedBy);

        assertEquals(specificDate, access.getGrantedAt());
    }

    @Test
    void should_return_all_values_correctly() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID grantedBy = UUID.randomUUID();
        LocalDateTime grantedAt = LocalDateTime.now();

        CustomerWarehouseAccess access = new CustomerWarehouseAccess(id, customerId, warehouseId, grantedAt, grantedBy);

        assertEquals(id, access.getId());
        assertEquals(customerId, access.getCustomerId());
        assertEquals(warehouseId, access.getWarehouseId());
        assertEquals(grantedAt, access.getGrantedAt());
        assertEquals(grantedBy, access.getGrantedBy());
    }
}
