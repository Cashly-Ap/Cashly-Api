# AUTH Module

## Overview
The AUTH module handles user authentication and authorization using JWT (JSON Web Tokens). It manages user registration, login, token refresh, and logout operations.

---

## Domain Model

### Entities

#### User
**Domain Entity**: `com.lexand.lexand_api.auth.domain.entities.User`

**Description**: Represents a system user with authentication credentials.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `UserId` (UUID) | Unique identifier | Required, immutable |
| `email` | `Email` | User email address | Required, immutable, valid email format |
| `userName` | `UserName` | Display name | Required, 2-50 characters |
| `password` | `Password` | Encrypted password | Required, min 8 characters |
| `active` | `boolean` | Account active status | Default: true |
| `emailVerified` | `boolean` | Email verification status | Default: false |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |

**Business Methods**:
- `changePassword(Password newPassword)` - Updates user password
- `updateUserName(UserName newUserName)` - Changes user name
- `activate()` - Activates the user account
- `deactivate()` - Deactivates the user account
- `verifyEmail()` - Marks email as verified
- `canAuthenticate()` - Checks if user can log in (must be active)
- `hasEmail(Email email)` - Validates if user has specific email

**Business Rules**:
1. User ID cannot be null
2. Email cannot be null and must be unique
3. Username cannot be null
4. Password cannot be null
5. Only active users can authenticate
6. All changes update the `updatedAt` timestamp

---

#### RefreshToken
**Domain Entity**: `com.lexand.lexand_api.auth.domain.entities.RefreshToken`

**Description**: Represents a refresh token for JWT authentication.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `RefreshTokenId` (UUID) | Unique identifier | Required, immutable |
| `userId` | `UserId` (UUID) | Associated user ID | Required, immutable |
| `token` | `String` | Token value | Required, non-empty |
| `expiresAt` | `LocalDateTime` | Expiration timestamp | Required, future date |
| `revoked` | `boolean` | Revocation status | Default: false |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |

**Business Methods**:
- `isExpired()` - Checks if token is expired
- `isValid()` - Checks if token is not revoked and not expired
- `revoke()` - Marks token as revoked
- `belongsToUser(UserId userId)` - Validates token ownership

**Business Rules**:
1. Token ID cannot be null
2. User ID cannot be null
3. Token string cannot be null or empty
4. Expiration date cannot be null or in the past
5. Revoked tokens cannot be reused
6. Expired tokens are invalid

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `UserId` | UUID | User unique identifier | Non-null UUID |
| `Email` | String | Email address | Valid email format, non-blank |
| `UserName` | String | Display name | 2-50 characters, non-blank |
| `Password` | String | User password | Min 8 characters, encrypted when stored |
| `RefreshTokenId` | UUID | Token unique identifier | Non-null UUID |

---

## API Endpoints

### 1. Register User
**Endpoint**: `POST /api/auth/register`

**Authentication**: Not required

**Description**: Creates a new user account.

**Request Body**:
```json
{
  "email": "user@example.com",
  "userName": "John Doe",
  "password": "SecurePassword123"
}
```

**Response** (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "userName": "John Doe",
  "active": true,
  "emailVerified": false,
  "createdAt": "2025-12-15T10:30:00"
}
```

**Errors**:
- `400 Bad Request`: Invalid request data
- `409 Conflict`: Email already exists

---

### 2. Login
**Endpoint**: `POST /api/auth/login`

**Authentication**: Not required

**Description**: Authenticates a user and returns JWT tokens.

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123"
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "userName": "John Doe",
    "active": true,
    "emailVerified": false,
    "createdAt": "2025-12-15T10:30:00"
  }
}
```

**Errors**:
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Invalid credentials
- `403 Forbidden`: User account is inactive

---

### 3. Refresh Token
**Endpoint**: `POST /api/auth/refresh`

**Authentication**: Not required (uses refresh token)

**Description**: Generates a new access token using a valid refresh token.

**Request Body**:
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "660e8400-e29b-41d4-a716-446655440111",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "userName": "John Doe",
    "active": true,
    "emailVerified": false,
    "createdAt": "2025-12-15T10:30:00"
  }
}
```

**Errors**:
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Invalid or expired refresh token

---

### 4. Logout
**Endpoint**: `POST /api/auth/logout`

**Authentication**: Not required (uses refresh token)

**Description**: Revokes a refresh token, effectively logging out the user.

**Request Body**:
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response** (204 No Content): Empty body

**Errors**:
- `400 Bad Request`: Invalid request data
- `404 Not Found`: Refresh token not found

---

## DTOs (Data Transfer Objects)

### Request DTOs

#### RegisterUserRequest
```java
public record RegisterUserRequest(
    String email,        // Required, valid email
    String userName,     // Required, non-blank
    String password      // Required, min 8 chars
)
```

#### LoginRequest
```java
public record LoginRequest(
    String email,        // Required, valid email
    String password      // Required, non-blank
)
```

#### RefreshTokenRequest
```java
public record RefreshTokenRequest(
    String refreshToken  // Required, non-blank
)
```

#### LogoutRequest
```java
public record LogoutRequest(
    String refreshToken  // Required, non-blank
)
```

---

### Response DTOs

#### UserResponse
```java
public record UserResponse(
    String id,                  // UUID as string
    String email,               // User email
    String userName,            // Display name
    boolean active,             // Account status
    boolean emailVerified,      // Email verification
    LocalDateTime createdAt     // Creation timestamp
)
```

#### AuthenticationResponse
```java
public record AuthenticationResponse(
    String accessToken,         // JWT access token
    String refreshToken,        // Refresh token UUID
    String tokenType,           // Always "Bearer"
    long expiresIn,            // Seconds until expiration
    UserResponse user          // User details
)
```

---

## Use Cases

### Application Layer Use Cases
1. **RegisterUserUseCase** - Creates a new user account
2. **LoginUserUseCase** - Authenticates user and generates tokens
3. **RefreshTokenUseCase** - Refreshes access token
4. **LogoutUserUseCase** - Revokes refresh token
5. **GetUserByIdUseCase** - Retrieves user by ID

---

## Repository Interfaces

### Domain Layer Ports
```java
public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
    boolean existsByEmail(Email email);
}

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(UserId userId);
}
```

---

## Domain Services

### AuthenticationDomainService
**Responsibilities**:
- Validate user credentials
- Generate JWT tokens
- Validate refresh tokens
- Password encryption/verification

---

## Security Configuration

### JWT Token Configuration
- **Access Token Expiration**: 1 hour (3600 seconds)
- **Refresh Token Expiration**: 7 days
- **Token Type**: Bearer
- **Algorithm**: HMAC SHA-256

### Password Encoding
- **Algorithm**: BCrypt
- **Strength**: 10 rounds

---

## Business Rules Summary

### User Registration
1. Email must be unique in the system
2. Email must be in valid format
3. Username must be between 2-50 characters
4. Password must be at least 8 characters
5. New users are active by default
6. New users have unverified email by default

### Authentication
1. Only active users can authenticate
2. Password must match encrypted stored password
3. Successful login generates both access and refresh tokens
4. Refresh tokens are single-use (revoked after refresh)

### Token Management
1. Access tokens expire after 1 hour
2. Refresh tokens expire after 7 days
3. Revoked tokens cannot be reused
4. Expired tokens are automatically invalid
5. Logout revokes the refresh token immediately

---

## Database Schema

### users Table
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    user_name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### refresh_tokens Table
```sql
CREATE TABLE refresh_tokens (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## Exception Handling

### Custom Exceptions
- `DuplicateEmailException` - Email already exists (409 Conflict)
- `InvalidCredentialsException` - Wrong email/password (401 Unauthorized)
- `InvalidTokenException` - Invalid JWT or refresh token (401 Unauthorized)
- `TokenExpiredException` - Expired token (401 Unauthorized)
- `UserNotFoundException` - User not found (404 Not Found)

---

## Notes for Developers

### When Creating New Features
1. **Domain Entities**: Use value objects for all fields, never primitives
2. **DTOs**: Keep them simple, use Java records
3. **Validation**: Validate in value objects and DTOs
4. **Password Handling**: Always encrypt, never log or expose
5. **Token Security**: Never expose refresh tokens in logs
6. **Timestamps**: Use `LocalDateTime` for all date/time fields

### Testing Requirements
- Unit tests for all value objects
- Unit tests for domain entities and their business methods
- Unit tests for use cases with mocked repositories
- Integration tests for controllers with test database
