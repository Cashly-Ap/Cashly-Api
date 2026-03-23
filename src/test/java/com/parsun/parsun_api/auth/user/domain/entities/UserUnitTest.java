package com.parsun.parsun_api.auth.user.domain.entities;

import com.parsun.parsun_api.auth.user.domain.valueobjects.CommissionDeductionPercentage;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.token.domain.valueobjects.RefreshTokenId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserUnitTest {

    private UserId userId;
    private Email email;
    private Password password;

    @BeforeEach
    void setUp() {
        userId = UserId.generate();
        email = new Email("test@example.com");
        password = Password.fromHash("$2a$12$hashedPassword");
    }

    @Test
    void should_CreateUser_When_ValidParametersProvided() {
        User user = new User(userId, email, new UserName("Test User"), password);

        assertNotNull(user);
        assertEquals(userId, user.getId());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertTrue(user.isActive());
        assertFalse(user.isEmailVerified());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void should_ThrowException_When_NullUserIdProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new User(null, email, new UserName("Test User"), password)
        );
        assertEquals("User ID cannot be null", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_NullEmailProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new User(userId, null, new UserName("Test User"), password)
        );
        assertEquals("Email cannot be null", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_NullUserNameProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new User(userId, email, null, password)
        );
        assertEquals("User name cannot be null", exception.getMessage());
    }

    @Test
    void should_ThrowException_When_NullPasswordProvided() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new User(userId, email, new UserName("Test User"), null)
        );
        assertEquals("Password cannot be null", exception.getMessage());
    }

    @Test
    void should_ChangePassword_When_NewPasswordProvided() {
        User user = new User(userId, email, new UserName("Test User"), password);
        Password newPassword = Password.fromHash("$2a$12$newHashedPassword");

        user.changePassword(newPassword);

        assertEquals(newPassword, user.getPassword());
    }

    @Test
    void should_ThrowException_When_NullPasswordProvidedForChange() {
        User user = new User(userId, email, new UserName("Test User"), password);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> user.changePassword(null)
        );
        assertEquals("New password cannot be null", exception.getMessage());
    }

    @Test
    void should_ActivateUser_When_ActivateMethodCalled() {
        User user = new User(userId, email, new UserName("Test User"), password);
        user.deactivate();

        user.activate();

        assertTrue(user.isActive());
    }

    @Test
    void should_DeactivateUser_When_DeactivateMethodCalled() {
        User user = new User(userId, email, new UserName("Test User"), password);

        user.deactivate();

        assertFalse(user.isActive());
    }

    @Test
    void should_VerifyEmail_When_VerifyEmailMethodCalled() {
        User user = new User(userId, email, new UserName("Test User"), password);

        user.verifyEmail();

        assertTrue(user.isEmailVerified());
    }

    @Test
    void should_ReturnTrue_When_UserCanAuthenticate() {
        User user = new User(userId, email, new UserName("Test User"), password);

        boolean canAuthenticate = user.canAuthenticate();

        assertTrue(canAuthenticate);
    }

    @Test
    void should_ReturnFalse_When_UserIsDeactivated() {
        User user = new User(userId, email, new UserName("Test User"), password);
        user.deactivate();

        boolean canAuthenticate = user.canAuthenticate();

        assertFalse(canAuthenticate);
    }

    @Test
    void should_ReturnTrue_When_UserHasGivenEmail() {
        User user = new User(userId, email, new UserName("Test User"), password);

        boolean hasEmail = user.hasEmail(email);

        assertTrue(hasEmail);
    }

    @Test
    void should_ReturnFalse_When_UserDoesNotHaveGivenEmail() {
        User user = new User(userId, email, new UserName("Test User"), password);
        Email differentEmail = new Email("other@example.com");

        boolean hasEmail = user.hasEmail(differentEmail);

        assertFalse(hasEmail);
    }

    @Test
    void should_ReturnFalse_When_NullEmailProvidedForHasEmail() {
        User user = new User(userId, email, new UserName("Test User"), password);

        boolean hasEmail = user.hasEmail(null);

        assertFalse(hasEmail);
    }

    @Test
    void should_BeEqual_When_SameUserIdProvided() {
        User user1 = new User(userId, email, new UserName("Test User"), password);
        User user2 = new User(userId, new Email("different@example.com"), new UserName("Other User"), password);

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentUserIdProvided() {
        User user1 = new User(UserId.generate(), email, new UserName("Test User"), password);
        User user2 = new User(UserId.generate(), email, new UserName("Test User"), password);

        assertNotEquals(user1, user2);
    }

    @Test
    void should_UpdateUserName_When_NewUserNameProvided() {
        User user = new User(userId, email, new UserName("Test User"), password);
        UserName newUserName = new UserName("Updated User");

        user.updateUserName(newUserName);

        assertEquals(newUserName, user.getUserName());
    }

    @Test
    void should_ThrowException_When_NullUserNameProvidedForUpdate() {
        User user = new User(userId, email, new UserName("Test User"), password);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> user.updateUserName(null)
        );
        assertEquals("User name cannot be null", exception.getMessage());
    }

    @Test
    void should_HaveDefaultCommissionDeductionPercentage_When_UserCreated() {
        User user = new User(userId, email, new UserName("Test User"), password);

        assertNotNull(user.getCommissionDeductionPercentage());
        assertEquals(0, user.getCommissionDeductionPercentage().value().compareTo(new BigDecimal("1.00")));
    }

    @Test
    void should_UpdateCommissionDeduction_When_ValidValueProvided() {
        User user = new User(userId, email, new UserName("Test User"), password);
        CommissionDeductionPercentage newDeduction = CommissionDeductionPercentage.of(new BigDecimal("2.50"));

        user.updateCommissionDeduction(newDeduction);

        assertEquals(0, user.getCommissionDeductionPercentage().value().compareTo(new BigDecimal("2.50")));
    }

    @Test
    void should_ThrowException_When_NullCommissionDeductionProvided() {
        User user = new User(userId, email, new UserName("Test User"), password);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> user.updateCommissionDeduction(null)
        );
        assertEquals("Commission deduction percentage cannot be null", exception.getMessage());
    }

    @Test
    void should_HidePasswordInToString_When_UserCreated() {
        User user = new User(userId, email, new UserName("Test User"), password);

        String result = user.toString();

        assertNotNull(result);
        assertTrue(result.contains("User"));
        assertTrue(result.contains("test@example.com"));
        assertTrue(result.contains("password=***"));
        assertFalse(result.contains("$2a$12$hashedPassword"));
    }
}
