package com.cashly.cashly_api.sales.orders.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderWarehouseReassignmentRemovalTest {
    @Test
    void should_not_expose_standalone_warehouse_reassignment()
            throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new OrderController(null,
                null, null, null, null, null, null, null)).build();

        mvc.perform(put("/api/orders/" + UUID.randomUUID() + "/allocations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lines\":[]}")).andExpect(status().isNotFound());
    }
}
