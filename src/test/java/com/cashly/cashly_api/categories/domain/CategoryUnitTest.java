package com.cashly.cashly_api.categories.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Category Domain Entity Unit Tests")
class CategoryUnitTest {

    private static final String VALID_NAME = "Test Category";
    private static final String VALID_DESCRIPTION = "A test category";

    

    @Test
    void should_CreateActiveCategory_With_NullId() {
        Category category = Category.create(VALID_NAME, VALID_DESCRIPTION);

        assertNull(category.getId());
        assertEquals("Test Category", category.getName());
        assertEquals("A test category", category.getDescription());
        assertTrue(category.isActive());
        assertNotNull(category.getCreatedAt());
        assertNotNull(category.getUpdatedAt());
    }

    

    @Test
    void should_Reconstitute_With_FullState() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Category category = Category.reconstitute(id, "Reconstituted", "Desc", false, now, now);

        assertEquals(id, category.getId());
        assertEquals("Reconstituted", category.getName());
        assertEquals("Desc", category.getDescription());
        assertFalse(category.isActive());
        assertEquals(now, category.getCreatedAt());
        assertEquals(now, category.getUpdatedAt());
    }

    

    @Test
    void should_UpdateNameDescriptionAndActive_When_ValidData() throws InterruptedException {
        Category category = Category.create(VALID_NAME, VALID_DESCRIPTION);
        LocalDateTime beforeUpdate = category.getUpdatedAt();

        Thread.sleep(10);
        category.update("Updated Name", "Updated Desc", false);

        assertEquals("Updated Name", category.getName());
        assertEquals("Updated Desc", category.getDescription());
        assertFalse(category.isActive());
        assertTrue(category.getUpdatedAt().isAfter(beforeUpdate));
    }

    @Test
    void should_NotChangeActive_When_ActiveIsNull() {
        Category category = Category.create(VALID_NAME, VALID_DESCRIPTION);
        category.deactivate();

        category.update("Updated Name", "Updated Desc", null);

        assertFalse(category.isActive());
        assertEquals("Updated Name", category.getName());
    }

    

    @Test
    void should_Activate_When_Deactivated() throws InterruptedException {
        Category category = Category.create(VALID_NAME, VALID_DESCRIPTION);
        category.deactivate();
        LocalDateTime beforeActivate = category.getUpdatedAt();

        Thread.sleep(10);
        category.activate();

        assertTrue(category.isActive());
        assertTrue(category.getUpdatedAt().isAfter(beforeActivate));
    }

    @Test
    void should_Deactivate_When_Active() throws InterruptedException {
        Category category = Category.create(VALID_NAME, VALID_DESCRIPTION);
        LocalDateTime beforeDeactivate = category.getUpdatedAt();

        Thread.sleep(10);
        category.deactivate();

        assertFalse(category.isActive());
        assertTrue(category.getUpdatedAt().isAfter(beforeDeactivate));
    }

    

    @Test
    void should_ThrowException_When_NameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                Category.create(null, VALID_DESCRIPTION));
    }

    @Test
    void should_ThrowException_When_NameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                Category.create("   ", VALID_DESCRIPTION));
    }

    @Test
    void should_ThrowException_When_NameIs1Char() {
        assertThrows(IllegalArgumentException.class, () ->
                Category.create("A", VALID_DESCRIPTION));
    }

    @Test
    void should_ThrowException_When_NameIs101Chars() {
        String longName = "a".repeat(101);

        assertThrows(IllegalArgumentException.class, () ->
                Category.create(longName, VALID_DESCRIPTION));
    }

    @Test
    void should_AcceptName_When_2Chars() {
        Category category = Category.create("AB", VALID_DESCRIPTION);

        assertEquals("AB", category.getName());
    }

    @Test
    void should_AcceptName_When_100Chars() {
        String maxName = "a".repeat(100);

        Category category = Category.create(maxName, VALID_DESCRIPTION);

        assertEquals(maxName, category.getName());
    }

    

    @Test
    void should_AcceptNullDescription() {
        Category category = Category.create(VALID_NAME, null);

        assertNull(category.getDescription());
    }

    @Test
    void should_AcceptDescription_When_300Chars() {
        String maxDesc = "d".repeat(300);

        Category category = Category.create(VALID_NAME, maxDesc);

        assertEquals(maxDesc, category.getDescription());
    }

    @Test
    void should_ThrowException_When_DescriptionIs301Chars() {
        String longDesc = "d".repeat(301);

        assertThrows(IllegalArgumentException.class, () ->
                Category.create(VALID_NAME, longDesc));
    }

    

    @Test
    void should_BeEqual_When_SameId() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Category c1 = Category.reconstitute(id, "Name 1", "Desc 1", true, now, now);
        Category c2 = Category.reconstitute(id, "Name 2", "Desc 2", false, now, now);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentIds() {
        LocalDateTime now = LocalDateTime.now();
        Category c1 = Category.reconstitute(UUID.randomUUID(), VALID_NAME, VALID_DESCRIPTION, true, now, now);
        Category c2 = Category.reconstitute(UUID.randomUUID(), VALID_NAME, VALID_DESCRIPTION, true, now, now);

        assertNotEquals(c1, c2);
    }

    @Test
    void should_BeEqual_When_BothIdsNull() {
        Category c1 = Category.create(VALID_NAME, VALID_DESCRIPTION);
        Category c2 = Category.create(VALID_NAME, VALID_DESCRIPTION);

        assertEquals(c1, c2);
    }
}
