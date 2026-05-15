package com.parsun.parsun_api.quotes.domain;

import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteLineId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuoteLineIdTest {

    @Test
    void should_generate_non_null_uuid() {
        QuoteLineId id = QuoteLineId.generate();
        assertNotNull(id);
        assertNotNull(id.value());
    }

    @Test
    void should_generate_unique_ids() {
        QuoteLineId id1 = QuoteLineId.generate();
        QuoteLineId id2 = QuoteLineId.generate();
        assertNotEquals(id1, id2);
    }

    @Test
    void should_create_from_valid_uuid() {
        UUID uuid = UUID.randomUUID();
        QuoteLineId id = QuoteLineId.of(uuid);
        assertEquals(uuid, id.value());
    }

    @Test
    void should_be_equal_for_same_uuid() {
        UUID uuid = UUID.randomUUID();
        QuoteLineId id1 = QuoteLineId.of(uuid);
        QuoteLineId id2 = QuoteLineId.of(uuid);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_uuids() {
        QuoteLineId id1 = QuoteLineId.of(UUID.randomUUID());
        QuoteLineId id2 = QuoteLineId.of(UUID.randomUUID());
        assertNotEquals(id1, id2);
    }

    @Test
    void generate_should_return_different_ids() {
        assertNotEquals(QuoteLineId.generate(), QuoteLineId.generate());
    }
}
