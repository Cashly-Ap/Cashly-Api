package com.cashly.cashly_api.sales.quotes.domain.valueobjects;

import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteAllocationId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuoteAllocationIdTest {

    @Test
    void should_generate_non_null_uuid() {
        QuoteAllocationId id = QuoteAllocationId.generate();
        assertNotNull(id);
        assertNotNull(id.value());
    }

    @Test
    void should_generate_unique_ids() {
        QuoteAllocationId id1 = QuoteAllocationId.generate();
        QuoteAllocationId id2 = QuoteAllocationId.generate();
        assertNotEquals(id1, id2);
    }

    @Test
    void should_create_from_valid_uuid() {
        UUID uuid = UUID.randomUUID();
        QuoteAllocationId id = QuoteAllocationId.of(uuid);
        assertEquals(uuid, id.value());
    }

    @Test
    void should_be_equal_for_same_uuid() {
        UUID uuid = UUID.randomUUID();
        QuoteAllocationId id1 = QuoteAllocationId.of(uuid);
        QuoteAllocationId id2 = QuoteAllocationId.of(uuid);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_uuids() {
        QuoteAllocationId id1 = QuoteAllocationId.of(UUID.randomUUID());
        QuoteAllocationId id2 = QuoteAllocationId.of(UUID.randomUUID());
        assertNotEquals(id1, id2);
    }

    @Test
    void generate_should_return_different_ids() {
        assertNotEquals(QuoteAllocationId.generate(),
                QuoteAllocationId.generate());
    }
}
