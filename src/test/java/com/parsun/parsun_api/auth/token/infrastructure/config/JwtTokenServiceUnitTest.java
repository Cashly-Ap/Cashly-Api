package com.parsun.parsun_api.auth.token.infrastructure.config;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("JwtTokenService Unit Tests")
class JwtTokenServiceUnitTest {

    private JwtTokenService tokenService;
    private User testUser;
    private Authenticable testAuthenticable;

    @BeforeEach
    void setUp() {
        String secret = "testSecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmSecurityPurposes";
        long accessTokenExpiration = 900000; 
        long refreshTokenExpiration = 604800000; 

        tokenService = new JwtTokenService(secret, accessTokenExpiration, refreshTokenExpiration);

        UserId userId = UserId.generate();
        Email email = new Email("test@example.com");
        Password password = Password.fromHash("$2a$10$hashedPassword");

        testUser = new User(userId, email, new UserName("Test User"), password);

        testAuthenticable = mock(Authenticable.class);
        when(testAuthenticable.getSubjectId()).thenReturn(userId.getValue().toString());
        when(testAuthenticable.getEmail()).thenReturn("test@example.com");
        when(testAuthenticable.getDisplayName()).thenReturn("Test User");
        when(testAuthenticable.getType()).thenReturn(AuthenticableType.USER);
        when(testAuthenticable.isActive()).thenReturn(true);
        when(testAuthenticable.getCreatedAt()).thenReturn(LocalDateTime.now());
    }

    @Test
    @DisplayName("should_GenerateAccessToken_When_ValidUserProvided")
    void should_GenerateAccessToken_When_ValidUserProvided() {
        String token = tokenService.generateAccessToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); 
    }

    @Test
    @DisplayName("should_GenerateAccessToken_When_ValidAuthenticableProvided")
    void should_GenerateAccessToken_When_ValidAuthenticableProvided() {
        String token = tokenService.generateAccessToken(testAuthenticable);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("should_ThrowException_When_GeneratingAccessTokenWithNullUser")
    void should_ThrowException_When_GeneratingAccessTokenWithNullUser() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> tokenService.generateAccessToken((User) null)
        );

        assertEquals("User cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_GeneratingAccessTokenWithNullAuthenticable")
    void should_ThrowException_When_GeneratingAccessTokenWithNullAuthenticable() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> tokenService.generateAccessToken((Authenticable) null)
        );

        assertEquals("Authenticable cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("should_GenerateRefreshToken_When_ValidUserProvided")
    void should_GenerateRefreshToken_When_ValidUserProvided() {
        RefreshToken refreshToken = tokenService.generateRefreshToken(testUser);

        assertNotNull(refreshToken);
        assertNotNull(refreshToken.getId());
        assertNotNull(refreshToken.getToken());
        assertNotNull(refreshToken.getExpiresAt());
        assertEquals(testUser.getId().getValue().toString(), refreshToken.getSubjectId());
        assertEquals(AuthenticableType.USER, refreshToken.getSubjectType());
        assertFalse(refreshToken.isRevoked());
        assertTrue(refreshToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("should_GenerateRefreshToken_When_ValidAuthenticableProvided")
    void should_GenerateRefreshToken_When_ValidAuthenticableProvided() {
        RefreshToken refreshToken = tokenService.generateRefreshToken(testAuthenticable);

        assertNotNull(refreshToken);
        assertNotNull(refreshToken.getId());
        assertNotNull(refreshToken.getToken());
        assertNotNull(refreshToken.getExpiresAt());
        assertEquals(testUser.getId().getValue().toString(), refreshToken.getSubjectId());
        assertEquals(AuthenticableType.USER, refreshToken.getSubjectType());
        assertFalse(refreshToken.isRevoked());
        assertTrue(refreshToken.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("should_ThrowException_When_GeneratingRefreshTokenWithNullUser")
    void should_ThrowException_When_GeneratingRefreshTokenWithNullUser() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> tokenService.generateRefreshToken((User) null)
        );

        assertEquals("User cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("should_ThrowException_When_GeneratingRefreshTokenWithNullAuthenticable")
    void should_ThrowException_When_GeneratingRefreshTokenWithNullAuthenticable() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> tokenService.generateRefreshToken((Authenticable) null)
        );

        assertEquals("Authenticable cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("should_ValidateToken_When_ValidTokenProvided")
    void should_ValidateToken_When_ValidTokenProvided() {
        String token = tokenService.generateAccessToken(testUser);

        boolean isValid = tokenService.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("should_ReturnFalse_When_ValidatingNullToken")
    void should_ReturnFalse_When_ValidatingNullToken() {
        boolean isValid = tokenService.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("should_ReturnFalse_When_ValidatingEmptyToken")
    void should_ReturnFalse_When_ValidatingEmptyToken() {
        boolean isValid = tokenService.validateToken("");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("should_ReturnFalse_When_ValidatingInvalidToken")
    void should_ReturnFalse_When_ValidatingInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = tokenService.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("should_ExtractUserId_When_ValidTokenProvided")
    void should_ExtractUserId_When_ValidTokenProvided() {
        String token = tokenService.generateAccessToken(testUser);

        UserId extractedUserId = tokenService.extractUserId(token);

        assertNotNull(extractedUserId);
        assertEquals(testUser.getId(), extractedUserId);
    }

    @Test
    @DisplayName("should_ExtractSubjectId_When_ValidTokenProvided")
    void should_ExtractSubjectId_When_ValidTokenProvided() {
        String token = tokenService.generateAccessToken(testAuthenticable);

        String extractedSubjectId = tokenService.extractSubjectId(token);

        assertNotNull(extractedSubjectId);
        assertEquals(testUser.getId().getValue().toString(), extractedSubjectId);
    }

    @Test
    @DisplayName("should_ExtractSubjectType_When_ValidTokenProvided")
    void should_ExtractSubjectType_When_ValidTokenProvided() {
        String token = tokenService.generateAccessToken(testAuthenticable);

        AuthenticableType extractedType = tokenService.extractSubjectType(token);

        assertNotNull(extractedType);
        assertEquals(AuthenticableType.USER, extractedType);
    }

    @Test
    @DisplayName("should_ThrowException_When_ExtractingUserIdFromNullToken")
    void should_ThrowException_When_ExtractingUserIdFromNullToken() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> tokenService.extractUserId(null)
        );

        assertEquals("Token cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("should_ExtractEmail_When_ValidTokenProvided")
    void should_ExtractEmail_When_ValidTokenProvided() {
        String token = tokenService.generateAccessToken(testUser);

        Email extractedEmail = tokenService.extractEmail(token);

        assertNotNull(extractedEmail);
        assertEquals(testUser.getEmail(), extractedEmail);
    }

    @Test
    @DisplayName("should_ThrowException_When_ExtractingEmailFromEmptyToken")
    void should_ThrowException_When_ExtractingEmailFromEmptyToken() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> tokenService.extractEmail("")
        );

        assertEquals("Token cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("should_GetExpirationDate_When_ValidTokenProvided")
    void should_GetExpirationDate_When_ValidTokenProvided() {
        String token = tokenService.generateAccessToken(testUser);

        LocalDateTime expirationDate = tokenService.getExpirationDate(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("should_ThrowException_When_GettingExpirationDateFromNullToken")
    void should_ThrowException_When_GettingExpirationDateFromNullToken() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> tokenService.getExpirationDate(null)
        );

        assertEquals("Token cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("should_ReturnAccessTokenExpiration_When_Requested")
    void should_ReturnAccessTokenExpiration_When_Requested() {
        long expiration = tokenService.getAccessTokenExpiration();

        assertEquals(900000, expiration);
    }

    @Test
    @DisplayName("should_ReturnRefreshTokenExpiration_When_Requested")
    void should_ReturnRefreshTokenExpiration_When_Requested() {
        long expiration = tokenService.getRefreshTokenExpiration();

        assertEquals(604800000, expiration);
    }

    @Test
    @DisplayName("should_GenerateDifferentTokens_When_CalledMultipleTimes")
    void should_GenerateDifferentTokens_When_CalledMultipleTimes() throws InterruptedException {
        String token1 = tokenService.generateAccessToken(testUser);
        Thread.sleep(1000); 
        String token2 = tokenService.generateAccessToken(testUser);

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("should_GenerateDifferentRefreshTokens_When_CalledMultipleTimes")
    void should_GenerateDifferentRefreshTokens_When_CalledMultipleTimes() {
        RefreshToken token1 = tokenService.generateRefreshToken(testUser);
        RefreshToken token2 = tokenService.generateRefreshToken(testUser);

        assertNotEquals(token1.getToken(), token2.getToken());
        assertNotEquals(token1.getId(), token2.getId());
    }

    @Test
    @DisplayName("should_IncludeUserDataInToken_When_TokenGenerated")
    void should_IncludeUserDataInToken_When_TokenGenerated() {
        String token = tokenService.generateAccessToken(testUser);

        UserId userId = tokenService.extractUserId(token);
        Email email = tokenService.extractEmail(token);

        assertEquals(testUser.getId(), userId);
        assertEquals(testUser.getEmail(), email);
    }

}
