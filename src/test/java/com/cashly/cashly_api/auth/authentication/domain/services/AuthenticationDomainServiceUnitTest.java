package com.cashly.cashly_api.auth.authentication.domain.services;

import com.cashly.cashly_api.auth.user.domain.entity.User;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.auth.token.domain.valueobjects.RefreshTokenId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationDomainServiceUnitTest {

    private AuthenticationDomainService authenticationDomainService;
    private User activeUser;
    private User inactiveUser;
    private User unverifiedUser;

    @BeforeEach
    void setUp() {
        authenticationDomainService = new AuthenticationDomainService();

        UserId userId1 = UserId.generate();
        Email email1 = new Email("active@example.com");
        UserName userName = new UserName("Test User");
        Password password = Password.fromHash("$2a$12$hashedPassword");

        activeUser = new User(userId1, email1, userName, password);
        activeUser.verifyEmail();

        UserId userId2 = UserId.generate();
        Email email2 = new Email("inactive@example.com");
        inactiveUser = new User(userId2, email2, userName, password);
        inactiveUser.deactivate();

        UserId userId3 = UserId.generate();
        Email email3 = new Email("unverified@example.com");
        unverifiedUser = new User(userId3, email3, userName, password);
    }

    @Test
    void should_ReturnTrue_When_ActiveUserCanAuthenticate() {
        boolean canAuthenticate = authenticationDomainService.canUserAuthenticate(activeUser);

        assertTrue(canAuthenticate);
    }

    @Test
    void should_ReturnFalse_When_InactiveUserCannotAuthenticate() {
        boolean canAuthenticate = authenticationDomainService.canUserAuthenticate(inactiveUser);

        assertFalse(canAuthenticate);
    }

    @Test
    void should_ThrowException_When_NullUserProvidedForCanAuthenticate() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationDomainService.canUserAuthenticate(null)
        );
        assertEquals("User cannot be null", exception.getMessage());
    }

    @Test
    void should_ReturnTrue_When_ActiveVerifiedUserCanPerformSensitiveOperations() {
        boolean canPerform = authenticationDomainService.canPerformSensitiveOperations(activeUser);

        assertTrue(canPerform);
    }

    @Test
    void should_ReturnFalse_When_InactiveUserCannotPerformSensitiveOperations() {
        boolean canPerform = authenticationDomainService.canPerformSensitiveOperations(inactiveUser);

        assertFalse(canPerform);
    }

    @Test
    void should_ReturnFalse_When_UnverifiedUserCannotPerformSensitiveOperations() {
        boolean canPerform = authenticationDomainService.canPerformSensitiveOperations(unverifiedUser);

        assertFalse(canPerform);
    }

    @Test
    void should_ThrowException_When_NullUserProvidedForSensitiveOperations() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationDomainService.canPerformSensitiveOperations(null)
        );
        assertEquals("User cannot be null", exception.getMessage());
    }
}
