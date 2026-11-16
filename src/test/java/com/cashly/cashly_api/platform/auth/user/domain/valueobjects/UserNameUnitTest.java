package com.cashly.cashly_api.platform.auth.user.domain.valueobjects;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserNameUnitTest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserNameUnitTest {

    @Test
    void should_CreateUserName_When_ValidValueProvided() {
        String validName = "John Doe";

        UserName userName = new UserName(validName);

        assertNotNull(userName);
        assertEquals(validName, userName.getValue());
    }

    @Test
    void should_TrimValue_When_CreatingUserName() {
        String nameWithSpaces = "  John Doe  ";

        UserName userName = new UserName(nameWithSpaces);

        assertEquals("John Doe", userName.getValue());
    }

    @Test
    void should_ThrowException_When_ValueIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new UserName(null));
    }

    @Test
    void should_ThrowException_When_ValueIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new UserName(""));
    }

    @Test
    void should_ThrowException_When_ValueIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new UserName("   "));
    }

    @Test
    void should_ThrowException_When_ValueIsTooShort() {
        String shortName = "A";

        assertThrows(IllegalArgumentException.class,
                () -> new UserName(shortName));
    }

    @Test
    void should_CreateUserName_When_ValueIsMinLength() {
        String minLengthName = "Jo";

        UserName userName = new UserName(minLengthName);

        assertNotNull(userName);
        assertEquals(minLengthName, userName.getValue());
    }

    @Test
    void should_ThrowException_When_ValueIsTooLong() {
        String longName = "A".repeat(101);

        assertThrows(IllegalArgumentException.class,
                () -> new UserName(longName));
    }

    @Test
    void should_CreateUserName_When_ValueIsMaxLength() {
        String maxLengthName = "A".repeat(100);

        UserName userName = new UserName(maxLengthName);

        assertNotNull(userName);
        assertEquals(maxLengthName, userName.getValue());
    }

    @Test
    void should_BeEqual_When_SameValue() {
        UserName userName1 = new UserName("John Doe");
        UserName userName2 = new UserName("John Doe");

        assertEquals(userName1, userName2);
        assertEquals(userName1.hashCode(), userName2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentValue() {
        UserName userName1 = new UserName("John Doe");
        UserName userName2 = new UserName("Jane Smith");

        assertNotEquals(userName1, userName2);
    }

    @Test
    void should_BeEqual_When_SameInstance() {
        UserName userName = new UserName("John Doe");

        assertEquals(userName, userName);
    }

    @Test
    void should_NotBeEqual_When_ComparedWithNull() {
        UserName userName = new UserName("John Doe");

        assertNotEquals(userName, null);
    }

    @Test
    void should_NotBeEqual_When_ComparedWithDifferentType() {
        UserName userName = new UserName("John Doe");
        String string = "John Doe";

        assertNotEquals(userName, string);
    }

    @Test
    void should_ReturnStringRepresentation_When_ToStringCalled() {
        UserName userName = new UserName("John Doe");

        String result = userName.toString();

        assertTrue(result.contains("UserName"));
        assertTrue(result.contains("John Doe"));
    }
}
